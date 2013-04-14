package io.escalante.play.subsystem

import org.jboss.msc.service._
import play.core.server.EmbeddedNettyServer
import java.io.File
import org.jboss.msc.inject.Injector
import java.util.concurrent.ExecutorService
import org.jboss.msc.value.InjectedValue
import io.escalante.logging.Log
import io.escalante.util.classload.ClassLoading
import org.jboss.logmanager.{Logger, LogContext}

/**
 * Netty server service for Play applications.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class PlayServerService(appPath: File, deploymentClassLoader: ClassLoader)
    extends Service[EmbeddedNettyServer] with Log {

  private val executor = new InjectedValue[ExecutorService]()

  private var server: EmbeddedNettyServer = _

  def start(ctx: StartContext) {
    debug("Starting Play server")
    ctx.asynchronous()
    executor.getValue.submit(new Runnable {
      def run() {
        // TODO: A slightly modified server required:
        // - thread pools need to be pluggable
        // - avoid adding runtime shutdown hooks
        debug(s"Start Netty server for Play application in $appPath")

        // TODO: Reloadable Play app should be supported!
        // Currently this is an issue because Play SBT plugin is needed, and
        // the latest version is compiled for Scala 2.9.2. Give it another
        // when go when a Scala 2.10 version of the Play SBT plugin is available:
        // http://repo.typesafe.com/typesafe/ivy-releases/play/sbt-plugin/
        // http://evgeny-goldin.com/wiki/Ivy-maven-plugin
        //
        // Even when Escalante implements a compile-on-the-fly mechanism
        // to compile Play SPI classes, the issue will still be present due
        // to the mix of Scala versions.
        //
        // Alternatively, find a way to mimic Play SBT plugin's work...
        // i.e. figuring out the resources to keep an eye on, or use create
        // a separate Scala 2.9.2 project for this and use reflection
        // (just like Play SBT plugin bridges over to Scala 2.10 classes?)
        //
        // server = NettyServer.mainDev()

        try {
          // TODO: Use a completely custom server instead of the NettyServer provided by Play
          // Several reasons:
          // - allows getting around the Logger configuration bug highlighted in
          //      https://groups.google.com/forum/#!searchin/play-framework/TRACE$20logging/play-framework/cAvr2vRtyI0/nUWY3oyW7eAJ
          // - allows for thread pools to be injected

          // Netty server needs to be created with the deployment classloader
          // as TCCL so that Akka can use the correct classloader to locate
          // the properties it needs, which are located in akka.jar/reference.conf
          ClassLoading.withContextClassLoader(deploymentClassLoader) {
            // TODO: Make ports and addresses configurable in deployment descriptor

            // Get root handlers before Play's Server trait constructor removes them!!
            // Pass them to constructor so that they can be reset upon construction
            val rootHandlers = Option(java.util.logging.Logger.getLogger(""))
                .map(_.getHandlers)

            server = new EmbeddedNettyServer(
              appPath, deploymentClassLoader,
              Option(System.getProperty("http.port")).map(Integer.parseInt(_)).getOrElse(9000),
              Option(System.getProperty("https.port")).map(Integer.parseInt(_)),
              Option(System.getProperty("http.address")).getOrElse("0.0.0.0"),
              rootHandlers
            )

            server.start()
          }
        }
        catch {
          case t: Throwable =>
            error(t, "Failed to start Play application")
            ctx.failed(new StartException("Failed to start Play server", t))
        }

        ctx.complete()
      }
    })
  }

  def stop(ctx: StopContext) {
    ctx.asynchronous()
    executor.getValue.submit(new Runnable {
      def run() {
        try {
          server.stop()
        } finally {
          ctx.complete()
        }
      }
    })
  }

  def getValue: EmbeddedNettyServer = server

  def executorInjector(): Injector[ExecutorService] = executor

}

object PlayServerService {

  def getServiceName(appName: String): ServiceName =
    ServiceName.of("escalante", "play", appName)

}
