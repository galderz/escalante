package org.scalabox.lift.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import snippet.HelloWorld
import org.junit.Test
import scala.xml.Elem
import org.scalabox.lift.AbstractLiftWebAppTest
import org.openqa.selenium.htmlunit.HtmlUnitDriver

/**
 * Base hello world test class.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractHelloWorldTest {

   @Test def testHelloWorld {
      val driver = new HtmlUnitDriver()
      driver.get("http://localhost:8080/helloworld/index.html")
      assert(driver.getPageSource.contains("Hello World!"))
   }

}

object AbstractHelloWorldTest extends AbstractLiftWebAppTest {

   def deployment(lift: Option[String], scala: Option[String]): WebArchive =
      deployment(lift, scala, classOf[HelloWorldBoot])

   def deployment(lift: Option[String], scala: Option[String],
           bootClass: Class[_ <: AnyRef]): WebArchive =
      deployment(lift, scala, bootClass,
         "org.scalabox.lift.helloworld.HelloWorldBoot",
         classOf[HelloWorld], classOf[AbstractHelloWorldTest])

   override val deploymentArchiveName: String = "helloworld.war"

   override val indexHtml: Elem =
      <lift:surround with="default" at="content">
         <h2>Welcome to ScalaBox!</h2>
         <p><lift:helloWorld.howdy /></p>
      </lift:surround>

   override val defaultHtml: Elem  =
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
