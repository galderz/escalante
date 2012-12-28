/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.{OperateOnDeployment, Deployment}
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test

/**
 * Hello world test with Scala 2.9.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala29Test extends AbstractHelloWorldTest {

  @Test
  @OperateOnDeployment("helloworld-default")
  def testHelloWorld() {
    helloWorld("default")
  }

  @Test
  @OperateOnDeployment("helloworld-291")
  def testHelloWorld291() {
    helloWorld("291")
  }

  @Test
  @OperateOnDeployment("helloworld-290")
  def testHelloWorld290() {
    helloWorld("290")
  }

}

object HelloWorldScala29Test {

  @Deployment(name = "helloworld-default", order = 1, testable = false)
  def deployment: WebArchive =
    deployHelloWorld(Some("2.4"), None)

  @Deployment(name = "helloworld-291", order = 2, testable = false)
  def deployment291: WebArchive =
      deployHelloWorld(Some("2.4"), Some("2.9.1"))

  @Deployment(name = "helloworld-290", order = 3, testable = false)
  def deployment290: WebArchive =
      deployHelloWorld(Some("2.4"), Some("2.9.0"))

  private[escalante] def deployHelloWorld(
      liftVersion: Option[String], scalaVersion: Option[String]): WebArchive = {
    scalaVersion match {
      case Some(scala) =>
        val descriptor =
          """
            | scala:
            |   version: %s
            | lift:
          """.format(scala).stripMargin

        HelloWorldTest.deployment("helloworld",
          "helloworld-%s.war".format(scala.replace(".", "")),
          descriptor, classOf[HelloWorldBoot])
      case None =>
        val descriptor =
          """
            | scala:
            | lift:
          """.stripMargin

        HelloWorldTest.deployment("helloworld", "helloworld-default.war",
          descriptor, classOf[HelloWorldBoot])
    }
  }

}

