package org.scalabox.lift.helloworld

import org.jboss.arquillian.junit.Arquillian
import org.junit.runner.RunWith
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.asset.{StringAsset, Asset}
import snippet.HelloWorld
import xml.Elem
import bootstrap.liftweb.Boot
import java.net.URL
import java.io.{BufferedInputStream, StringWriter}
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.jboss.shrinkwrap.api.{GenericArchive, ShrinkWrap}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldTest extends AssertionsForJUnit {

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
            assert(writer.toString.indexOf("Hello World!") > -1)
            println("OK")
         }
      }
   }

   def use[T <: { def close(): Unit }](closable: T)(block: T => Unit) {
      try {
         block(closable)
      }
      finally {
         closable.close()
      }
   }

}

object HelloWorldTest {

   @Deployment def deployment: WebArchive = {
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

      // TODO: How avoid duplicating library version?? Load from a pom...
      war.addAsWebResource(indexHtml, "index.html")
         .addAsWebResource(defaultHtml, "templates-hidden/default.html")
         .addClasses(classOf[Boot], classOf[HelloWorld])
         .addAsLibraries(DependencyResolvers.use(classOf[MavenDependencyResolver])
            .artifacts("org.scalatest:scalatest_2.9.0:1.6.1")
                  .exclusion("org.scala-lang:scala-library")
            .resolveAs(classOf[GenericArchive])
         )

      return war
   }

   private def xml(e: Elem): Asset = new StringAsset(e.toString())

}