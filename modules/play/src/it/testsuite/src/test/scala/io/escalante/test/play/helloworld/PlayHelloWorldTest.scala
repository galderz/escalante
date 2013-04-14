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
import io.escalante.test.play.PlayWebApp
import org.junit.{AfterClass, BeforeClass, Test}

/**
 * Hello World Play application test.
 *
 * TRACE logging for "play" category here is governed by the application
 * server's logging configuration in standalone.xml. This is because it uses
 * Slf4j logging. Since programmatic customization of the logging section
 * of standalone.xml is not yet in place, the easiest to get TRACE logging is
 * to modify [JBOSS_HOME]/standalone/configuration/standalone.xml.original
 * and the logging configuration will be picked.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class PlayHelloWorldTest {

  import PlayHelloWorldTest._

  // TODO: Ask David if something can be done to avoid relying on standalone.xml,
  // why doesn't logging.properties kick in here?

  @Test def testHelloWorld() {
    val driver = new HtmlUnitDriver()
    driver.get(s"http://localhost:9000/$AppName")
    assert(driver.getPageSource.contains("Hello (Escalante) Play!"))
  }

}

object PlayHelloWorldTest {

  val AppName = "helloworld"

  val webApp = new PlayWebApp(AppName)

  @BeforeClass
  def deploy() {
    webApp.deploy(withDb = false)
  }

  @AfterClass
  def undeploy() {
    webApp.undeploy()
  }

}
