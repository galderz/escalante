/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import io.escalante.io.FileSystem._
import io.escalante.test.play.{PlayConsole, PlayDescriptor}
import org.junit.{AfterClass, BeforeClass, Test}
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper
import java.io.File

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class PlayHelloWorldTest {

  @Test def testHelloWorld() {
    val driver = new HtmlUnitDriver()
    driver.get("http://localhost:9000/helloworld")
    assert(driver.getPageSource.contains("Hello (Escalante) Play!"))
  }

}

object PlayHelloWorldTest {

  val APP_NAME = "helloworld"

  val DEPLOY_NAME = s"$APP_NAME.yml"

  val APP_PATH = "modules/play/src/it/testsuite/src/test/applications/helloworld"

  val SERVER_CLIENT = new ServerDeploymentHelper(
    ModelControllerClient.Factory.create("localhost", 9999))

  @BeforeClass
  def deploy() {
    val descriptor = new PlayDescriptor(DEPLOY_NAME, APP_PATH)

    // Package static Play app (reloadable apps will be supported in future)
    PlayConsole.packageApp(APP_PATH)

    SERVER_CLIENT.deploy(DEPLOY_NAME, descriptor.exportAsStream)
  }

  @AfterClass
  def undeploy() {
    SERVER_CLIENT.undeploy(DEPLOY_NAME)
  }

}
