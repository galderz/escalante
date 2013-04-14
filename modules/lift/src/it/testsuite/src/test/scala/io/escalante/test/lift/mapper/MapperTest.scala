/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift.mapper

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.openqa.selenium.By
import collection.JavaConversions._
import io.escalante.test.lift.LiftWebApp
import io.escalante.lift.mapper.model.User

/**
 * Test for a Lift ORM model based application.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class MapperTest {

  val driver = new HtmlUnitDriver()

  @Test def testStaticContent() {
    driver.get(s"$appUrl/static/index")
    val source = driver.getPageSource
    assert(source.contains("Static content... everything you put in the /static"),
      "Instead, page source contains: " + source)
  }

  @Test def testUserSignUpAndLogin() {
    // Load sign up page
    driver.get(s"$appUrl/user_mgt/sign_up")
    // Fill in fields
    driver.findElement(By.id("txtFirstName")).sendKeys("Galder")
    driver.findElement(By.id("txtLastName")).sendKeys("Zamarreño")
    driver.findElement(By.id("txtEmail")).sendKeys("athletic@bilbao.com")
    collectionAsScalaIterable(driver.findElements(
      By.cssSelector("input[type='password']"))).foreach {
      elem =>
        elem.clear() // Clear the '*' from the field first
        elem.sendKeys("boomoo")
    }
    // Click on sign up
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // If sign up worked, 'Logout' should be found
    findLogout()

    // Click logout
    driver.get(s"$appUrl/user_mgt/logout")

    // Click on login and fill in details
    driver.get(s"$appUrl/user_mgt/login")
    driver.findElement(By.name("username")).sendKeys("athletic@bilbao.com")
    driver.findElement(By.name("password")).sendKeys("boomoo")
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // If sign up worked, 'Logout' should be found
    findLogout()
  }

  protected def appUrl: String = "http://localhost:8080/mapper-default"

  private def findLogout() {
    val source = driver.getPageSource
    assert(source.contains("Logout"), "Instead, page source contains: " + source)
  }

}

object MapperTest {

  @Deployment def deployment: WebArchive = {
    val descriptor =
      s"""
        | scala:
        | lift:
        |   version: ${LiftWebApp.LIFT_VERSION}
        |   modules:
        |     - mapper
      """.stripMargin

    createWebApp(descriptor, "mapper-default.war")
  }

  private def createWebApp(
      descriptor: String,
      deploymentName: String): WebArchive = {
    val indexHtml =
      <html>
        <head>
          <meta content="text/html; charset=UTF-8" http-equiv="content-type"/>
          <title>Home</title>
        </head>
        <body class="lift:content_id=main">
          <div id="main" class="lift:surround?with=default;at=content">
            <h2>Welcome to Escalante!</h2>
            <p>
              The home of Scala apps :)
            </p>
          </div>
        </body>
      </html>

    LiftWebApp(
      "mapper",
      deploymentName,
      descriptor,
      classOf[MapperBoot],
      List(classOf[User], classOf[MapperTest]),
      Map(
        "templates-hidden/default.html" -> "",
        "templates-hidden/wizard-all.html" -> "",
        "static/index.html" -> ""
      ),
      indexHtml)
  }

}
