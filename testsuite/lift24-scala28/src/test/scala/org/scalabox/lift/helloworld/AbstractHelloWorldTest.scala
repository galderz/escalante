package org.scalabox.lift.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.shrinkwrap.api.asset.{StringAsset, Asset}
import snippet.HelloWorld
import java.net.URL
import java.io.{BufferedInputStream, StringWriter}
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.jboss.shrinkwrap.api.{GenericArchive, ShrinkWrap}
import org.scalabox.util.Closeable._
import org.scalabox.logging.Log
import org.junit.Test
import scala.xml.Attribute
import scala.xml.Text
import scala.xml.Null
import scala.xml.Elem
import org.scalabox.util.Closeable

/**
 * Base hello world test class.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractHelloWorldTest {

   @Test def testHelloWorld = performHttpCall("localhost", 8080, "helloworld")

   private def performHttpCall(host: String, port: Int, context: String) {
      use(new StringWriter) { writer =>
         val url = new URL("http://" + host + ":" + port + "/" + context + "/index.html")
         println("Reading response from " + url + ":")
         val con = url.openConnection
         use(new BufferedInputStream(con.getInputStream)) { in =>
            var i = in.read
            while (i != -1) {
               writer.write(i.asInstanceOf[Char])
               i = in.read
            }
            val rsp = writer.toString
            assert(rsp.indexOf("Hello World!") > -1, rsp)
            println("OK")
         }
      }
   }

}

object AbstractHelloWorldTest extends Log {

   def deployment(): WebArchive = deployment(None, None)

   def deployment(lift: Option[String], scala: Option[String]): WebArchive =
      deployment(lift, scala, classOf[HelloWorldBoot])

   def deployment(lift: Option[String], scala: Option[String], bootClass: Class[_ <: AnyRef]): WebArchive = {
      info("Create war deployment")

      val war = ShrinkWrap.create(classOf[WebArchive], "helloworld.war")
      val indexHtml = xml {
         <lift:surround with="default" at="content">
          <h2>Welcome to ScalaBox!</h2>
          <p><lift:helloWorld.howdy /></p>
         </lift:surround>
      }

      val defaultHtml = xml {
         <html xmlns="http://www.w3.org/1999/xhtml"
               xmlns:lift="http://liftweb.net/">
            <head>
              <meta http-equiv="content-type"
                    content="text/html; charset=UTF-8" />
              <meta name="description" content="" />
              <meta name="keywords" content="" />

              <title>org.scalabox.lift.helloworld:helloworld:1.0-SNAPSHOT</title>
              <script id="jquery" src="/classpath/jquery.js"
                      type="text/javascript"></script>
            </head>
            <body>
              <lift:bind name="content" />
              <lift:Menu.builder />
              <lift:msgs/>
            </body>
         </html>
      }

      val liftXml = xml {
         (lift, scala) match {
            case (Some(lift), Some(scala)) =>
               <lift-app/> %
                     Attribute(None, "version", Text(lift), Null) %
                     Attribute(None, "scala-version", Text(scala), Null)
            case (Some(lift), None) =>
               <lift-app/> % Attribute(None, "version", Text(lift), Null)
            case (None, Some(scala)) =>
               <lift-app/> % Attribute(None, "scala-version", Text(scala), Null)
            case (None, None) => <lift-app/>
         }
      }

      /**
       * This web.xml is not necessary, but it's added in order to provide a
       * custom boot class. This allows for multiple apps to be tested within
       * the same testsuite.
       */
      val webXml = xml {
         <web-app>
            <filter>
               <filter-name>LiftFilter</filter-name>
               <display-name>Lift Filter</display-name>
               <description>The Filter that intercepts lift calls</description>
               <filter-class>net.liftweb.http.LiftFilter</filter-class>
               <init-param>
                  <param-name>bootloader</param-name>
                  <param-value>org.scalabox.lift.helloworld.HelloWorldBoot</param-value>
               </init-param>
            </filter>
            <filter-mapping>
               <filter-name>LiftFilter</filter-name>
               <url-pattern>/*</url-pattern>
            </filter-mapping>
         </web-app>
      }

      // TODO: How avoid duplicating library version?? Load from a pom...
      war.addAsWebResource(indexHtml, "index.html")
         .addAsWebResource(defaultHtml, "templates-hidden/default.html")
         .addAsWebResource(liftXml, "WEB-INF/lift.xml")
         .addAsWebResource(webXml, "WEB-INF/web.xml")
         .addClasses(bootClass, classOf[Closeable],
               classOf[HelloWorld], classOf[AbstractHelloWorldTest])
         .addAsLibraries(DependencyResolvers.use(classOf[MavenDependencyResolver])
            .artifacts("org.scalatest:scalatest_2.9.0:1.7.1")
                  .exclusion("org.scala-lang:scala-library")
            .resolveAs(classOf[GenericArchive])
         )

      info("War deployment created")

      return war
   }

   private def xml(e: Elem): Asset = new StringAsset(e.toString())

}