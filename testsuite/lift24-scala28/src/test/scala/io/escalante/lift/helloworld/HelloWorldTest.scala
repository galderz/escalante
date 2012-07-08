package io.escalante.lift.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.{OperateOnDeployment, Deployment}
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import snippet.HelloWorld
import xml.Elem
import io.escalante.lift.AbstractLiftWebAppTest

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldTest extends AbstractHelloWorldTest {

   @Test @OperateOnDeployment("helloworld-282")
   def testHelloWorld() {
      helloWorld("282")
   }

   @Test @OperateOnDeployment("helloworld-281")
   def testHelloWorld281() {
      helloWorld("281")
   }

   @Test @OperateOnDeployment("helloworld-280")
   def testHelloWorld280() {
      helloWorld("280")
   }

}

object HelloWorldTest extends AbstractLiftWebAppTest {

   @Deployment(name = "helloworld-282", order = 1)
   def deployment: WebArchive = deployHelloWorld("2.8.2")

   @Deployment(name = "helloworld-281", order = 2)
   def deployment281: WebArchive = deployHelloWorld("2.8.1")

   @Deployment(name = "helloworld-280", order = 3)
   def deployment280: WebArchive = deployHelloWorld("2.8.0")

   private def deployHelloWorld(scala: String): WebArchive =
      deployment("helloworld", "helloworld-%s.war".format(scala.replace(".", "")),
         None, Some(scala), classOf[HelloWorldBoot])

   def deployment(appName: String, deployName: String,
           lift: Option[String], scala: Option[String],
           bootClass: Class[_ <: AnyRef]): WebArchive =
      deployment(appName, deployName, lift, scala, bootClass,
         "io.escalante.lift.helloworld.HelloWorldBoot",
         classOf[HelloWorld], classOf[HelloWorldTest],
         classOf[AbstractHelloWorldTest])

   override val indexHtml: Elem =
      <lift:surround with="default" at="content">
         <h2>Welcome to Escalante!</h2>
         <p><lift:helloWorld.howdy /></p>
      </lift:surround>

   override val templates: Seq[String] = List("templates-hidden/default.html")

   override val static: Option[Elem] = None

}
