package org.scalabox.lift.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import snippet.HelloWorld
import xml.Elem
import org.scalabox.lift.AbstractLiftWebAppTest

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldTest {

   @Test def testHelloWorld() {
      val driver = new HtmlUnitDriver()
      driver.get("http://localhost:8080/helloworld/index.html")
      assert(driver.getPageSource.contains("Hello World!"))
   }

}

object HelloWorldTest extends AbstractLiftWebAppTest {

   @Deployment def deployment: WebArchive =
      deployment(None, Some("2.8.2"), classOf[HelloWorldBoot])

   def deployment(lift: Option[String], scala: Option[String],
           bootClass: Class[_ <: AnyRef]): WebArchive =
      deployment(lift, scala, bootClass,
         "org.scalabox.lift.helloworld.HelloWorldBoot",
         classOf[HelloWorld], classOf[HelloWorldTest])

   override val appName: String = "helloworld"

   override val indexHtml: Elem =
      <lift:surround with="default" at="content">
         <h2>Welcome to ScalaBox!</h2>
         <p><lift:helloWorld.howdy /></p>
      </lift:surround>

   override val templates: Seq[String] = List("templates-hidden/default.html")

   override val static: Option[Elem] = None

}
