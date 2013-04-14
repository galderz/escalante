/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play.persistdb

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.{AfterClass, BeforeClass, Test}
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import io.escalante.test.play.PlayWebApp
import org.openqa.selenium.{WebDriver, By}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class PersistToDatabaseTest {

  import PersistToDatabaseTest._

  @Test def testAddTask() {
    val driver = new HtmlUnitDriver()
    driver.get(s"http://localhost:9000/$AppName")
    assertSourceContains(driver, "Add a new task")
    assertSourceContains(driver, "0 task(s)")

    // Add a new task and click on create button
    driver.findElement(By.id("label")).sendKeys("Deploy to Escalante")
    driver.findElement(By.cssSelector("input[type='submit']")).click()
    assertSourceContains(driver, "1 task(s)")
    assertSourceContains(driver, "Deploy to Escalante")

    // Delete newly created task
    driver.findElement(By.cssSelector("input[value='Delete']")).click()
    assertSourceContains(driver, "0 task(s)")
  }

  def assertSourceContains(driver: WebDriver, expectedTxt: String) {
    val source = driver.getPageSource
    assert(source.contains(expectedTxt),
      "Instead, page source contains: " + source)
  }

}

object PersistToDatabaseTest {

  val AppName = "persistdb"

  val webApp = new PlayWebApp(AppName)

  @BeforeClass
  def deploy() {
    webApp.deploy(withDb = true)
  }

  @AfterClass
  def undeploy() {
    webApp.undeploy()
  }

}
