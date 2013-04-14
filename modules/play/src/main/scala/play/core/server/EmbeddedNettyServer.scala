/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package play.core.server

import java.io.{FileInputStream, File}
import java.net.InetSocketAddress
import java.security.KeyStore
import java.util.concurrent.Executors
import java.util.logging.{Handler, Level}
import javax.net.ssl.{TrustManager, KeyManagerFactory, SSLContext}
import netty.{PlayDefaultUpstreamHandler, FakeKeyStore}
import org.jboss.netty.bootstrap.ServerBootstrap
import org.jboss.netty.channel.ChannelPipelineFactory
import org.jboss.netty.channel.Channels._
import org.jboss.netty.channel.group.DefaultChannelGroup
import org.jboss.netty.handler.codec.http.{HttpContentDecompressor, HttpResponseEncoder, HttpRequestDecoder}
import org.jboss.netty.handler.ssl.SslHandler
import play.api._
import play.core.ApplicationProvider
import play.core.NamedThreadFactory
import scala.{Array, Some}

/**
 * Custom Netty server for Play applications, based on
 * [[play.core.server.NettyServer]] code, designed to address several
 * requirements:
 *
 * 1. Avoid the Logger bug in this forum post by changing the order in which
 * the Server's logger configuration and the application's logger
 * configuration are executed. If the server's is executed first, the problem
 * is avoided. <-- TODO: Is this still the case? Server configuration dictating logging settings
 *
 * 2. Inject application server thread pools to Netty server so that they
 * can be managed by the app server.
 *
 * Note that this server in the <code>play.core.server</code> on purpouse,
 * so that it can access the [[play.core.server.netty.PlayDefaultUpstreamHandler]]
 * class.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
class EmbeddedNettyServer(
    appPath: File,
    deploymentClassLoader: ClassLoader,
    port: Int,
    sslPort: Option[Int] = None,
    address: String = "0.0.0.0",
    rootHandlers: Option[Array[Handler]],
    val mode: Mode.Mode = Mode.Prod) extends Server with ServerWithStop {

  lazy val appProvider = new DeploymentStaticApplication(rootHandlers)

  // Keep a reference on all opened channels (useful to close everything properly, especially in DEV mode)
  val allChannels = new DefaultChannelGroup

  // Our upStream handler is stateless. Let's use this instance for every new connection
  val defaultUpStreamHandler = new PlayDefaultUpstreamHandler(this, allChannels)

  override lazy val mainAddress = HTTP._2.getLocalAddress.asInstanceOf[InetSocketAddress]

  // The HTTP server channel
  val HTTP = {
    val bootstrap = newBootstrap
    bootstrap.setPipelineFactory(new PlayPipelineFactory)
    val channel = bootstrap.bind(new InetSocketAddress(address, port))
    allChannels.add(channel)
    (bootstrap, channel)
  }

  // Maybe the HTTPS server channel
  val HTTPS = sslPort.map { port =>
    val bootstrap = newBootstrap
    bootstrap.setPipelineFactory(new PlayPipelineFactory(secure = true))
    val channel = bootstrap.bind(new InetSocketAddress(address, port))
    allChannels.add(channel)
    (bootstrap, channel)
  }

  def applicationProvider: ApplicationProvider = appProvider

  // TODO: Use AS thread pools...
  def newBootstrap = new ServerBootstrap(
    new org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory(
      Executors.newCachedThreadPool(NamedThreadFactory("netty-boss")),
      Executors.newCachedThreadPool(NamedThreadFactory("netty-worker"))))

  class PlayPipelineFactory(secure: Boolean = false) extends ChannelPipelineFactory {

    def getPipeline = {
      val newPipeline = pipeline()
      if (secure) {
        sslContext.map { ctxt =>
          val sslEngine = ctxt.createSSLEngine
          sslEngine.setUseClientMode(false)
          newPipeline.addLast("ssl", new SslHandler(sslEngine))
        }
      }
      newPipeline.addLast("decoder", new HttpRequestDecoder(4096, 8192, 8192))
      newPipeline.addLast("encoder", new HttpResponseEncoder())
      newPipeline.addLast("decompressor", new HttpContentDecompressor())
      newPipeline.addLast("handler", defaultUpStreamHandler)
      newPipeline
    }

    lazy val sslContext: Option[SSLContext] =  //the sslContext should be reused on each connection
      Option(System.getProperty("https.keyStore")) map { path =>
      // Load the configured key store
        val keyStore = KeyStore.getInstance(System.getProperty("https.keyStoreType", "JKS"))
        val password = System.getProperty("https.keyStorePassword", "").toCharArray
        val algorithm = System.getProperty("https.keyStoreAlgorithm", KeyManagerFactory.getDefaultAlgorithm)
        val file = new File(path)
        if (file.isFile) {
          for (in <- resource.managed(new FileInputStream(file))) {
            keyStore.load(in, password)
          }
          Logger("play").debug("Using HTTPS keystore at " + file.getAbsolutePath)
          try {
            val kmf = KeyManagerFactory.getInstance(algorithm)
            kmf.init(keyStore, password)
            Some(kmf)
          } catch {
            case e: Exception => {
              Logger("play").error("Error loading HTTPS keystore from " + file.getAbsolutePath, e)
              None
            }
          }
        } else {
          Logger("play").error("Unable to find HTTPS keystore at \"" + file.getAbsolutePath + "\"")
          None
        }
      } orElse {

        // Load a generated key store
        Logger("play").warn("Using generated key with self signed certificate for HTTPS. This should not be used in production.")
        Some(FakeKeyStore.keyManagerFactory(applicationProvider.path))

      } flatMap { a => a } map { kmf =>
      // Load the configured trust manager
        val tm = Option(System.getProperty("https.trustStore")).map {
          case "noCA" => {
            Logger("play").warn("HTTPS configured with no client " +
                "side CA verification. Requires http://webid.info/ for client certifiate verification.")
            Array[TrustManager](noCATrustManager)
          }
          case _ => {
            Logger("play").debug("Using default trust store for client side CA verification")
            null
          }
        }.getOrElse {
          Logger("play").debug("Using default trust store for client side CA verification")
          null
        }

        // Configure the SSL context
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(kmf.getKeyManagers, tm, null)
        sslContext
      }
  }

  /**
   * TODO: Is start() necessary any more? Logging configured via server configuration, i.e. standalone.xml
   *
   * Start method added in order to get around the Logger bug described in
   * this <a href="https://groups.google.com/forum/#!searchin/play-framework/TRACE$20logging/play-framework/cAvr2vRtyI0/nUWY3oyW7eAJ">forum post</a>.
   * The issue is related to the order in which Loggers are configured.
   * For the Logger to be configured correctly, the server's logger
   * configuration needs to run before the application's logger. This
   * can achieve by delaying the application's logger initialization to a
   * start phase, taking in account that the server initializes logging in
   * construction.
   */
  def start() {
    // Initialise application provider, which will configure the Logger accordingly.
    appProvider.start()

    mode match {
      case Mode.Test =>
      case _ => {
        Logger("play").info("Listening for HTTP on %s".format(HTTP._2.getLocalAddress))
        HTTPS.foreach { https =>
          Logger("play").info("Listening for HTTPS on port %s".format(https._2.getLocalAddress))
        }
      }
    }
  }

  override def stop() {
    try {
      Play.stop()
    } catch {
      case t: Throwable => Logger("play").error("Error while stopping the application", t)
    }

    mode match {
      case Mode.Test =>
      case _ => Logger("play").info("Stopping server...")
    }

    // First, close all opened sockets
    allChannels.close().awaitUninterruptibly()

    // Release the HTTP server
    HTTP._1.releaseExternalResources()

    // Release the HTTPS server if needed
    HTTPS.foreach(_._1.releaseExternalResources())
  }

  /**
   * Defines how a static Play application deployed in Escalante gets
   * initialised. It uses the application path provided in the deployment
   * descriptor, and the [[java.lang.ClassLoader]] of the web app where
   * it can locate classes and configuration files.
   */
  class DeploymentStaticApplication(
      val rootHandlers: Option[Array[Handler]]) extends ApplicationProvider {

    // TODO: Mode.Prod creates problems with multiple application deployment (with undeployment in the middle)
    // After deploying an application, Configuration.dontAllowMissingConfig
    // remains cached with the previous application's configuration, hence you
    // get 404 when accessing the new application. Mod.Dev might help with this...

    lazy val application = new DefaultApplication(
      appPath, deploymentClassLoader, None, Mode.Dev)

    def get: Either[Throwable, Application] = Right(application)

    def path: File = appPath

    def start() {
      val localApplication = application
      resetRootHandlers() // Restore sanity...
      Play.start(localApplication)
    }

    private def resetRootHandlers() {
      for {
        rootHandler <- rootHandlers
      } yield {
        val root = java.util.logging.Logger.getLogger("")
        rootHandler.foreach(root.addHandler(_))
      }
    }

  }

}
