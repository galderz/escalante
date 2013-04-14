/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.{OperateOnDeployment, Deployment}
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import snippet.HelloWorld
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import io.escalante.Scala
import io.escalante.test.lift.LiftWebApp

/**
 * Tests a basic Lift web application which returns the "Hello World!" message.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class LiftHelloWorldTest {

  @Test
  @OperateOnDeployment("helloworld-default")
  def testHelloWorld() {
    helloWorld("default")
  }

  private def helloWorld(appVersion: String) {
    val driver = new HtmlUnitDriver()
    driver.get(s"http://localhost:8080/helloworld-$appVersion/index.html")
    assert(driver.getPageSource.contains("Hello World!"))
  }

}

object LiftHelloWorldTest {

  @Deployment(name = "helloworld-default", order = 1)
  def deployment: WebArchive = deployment(None)

  private def deployment(scalaVersion: Option[Scala]): WebArchive = {
    scalaVersion match {
      case None =>
        val descriptor =
          s"""
            | scala:
            | lift:
            |   version: ${LiftWebApp.LIFT_VERSION}
          """.stripMargin

        createWebApp(descriptor, "helloworld-default.war")
      case Some(scala) =>
        val descriptor =
          s"""
            | scala:
            |   version: ${scala.version}
            | lift:
            |   version: ${LiftWebApp.LIFT_VERSION}
          """.stripMargin

        createWebApp(descriptor, s"helloworld-${scala.urlVersion}.war")
    }
  }

  private def createWebApp(
      descriptor: String,
      deploymentName: String): WebArchive = {
    val indexHtml =
      <lift:surround with="default" at="content">
        <h2>Welcome to Escalante!</h2>
        <p>
          <lift:helloWorld.howdy/>
        </p>
      </lift:surround>

    LiftWebApp(
      "helloworld",
      deploymentName,
      descriptor,
      classOf[HelloWorldBoot],
      List(classOf[HelloWorld], classOf[LiftHelloWorldTest]),
      Map("templates-hidden/default.html" -> ""),
      indexHtml)
  }

}
