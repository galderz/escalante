/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.jpa

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By
import io.escalante.Scala
import io.escalante.test.lift.LiftWebApp
import snippet.{Books, Authors}

/**
 * Library JPA test
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class JpaTest {

  protected def appUrl: String = "http://localhost:8080/jpa-default"

  @Test def testAddAuthorAndBook() {
    val driver = new HtmlUnitDriver()

    // 1. Go to index.html and check it says: "JEE Examples"
    driver.get(appUrl)
    assert(driver.getPageSource.contains("JEE Examples"))

    // 2. Add an author
    driver.get(s"$appUrl/authors/add")
    driver.findElement(By.cssSelector("input[type='text']")).sendKeys("Galder")
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // 3. Check if author list contains the added author
    driver.get(s"$appUrl/authors/index")
    assert(driver.getPageSource.contains("Galder"))
    assert(driver.getPageSource.contains("0 books"))

    // 4. Add a new Book with the added author
    driver.get(s"$appUrl/books/add")
    // First text input is Title
    driver.findElement(By.cssSelector("input[type='text']")).sendKeys("Escalante In Action")
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // 5. Check if the books for the author have been updated
    driver.get(s"$appUrl/authors")
    assert(driver.getPageSource.contains("Galder"))
    assert(driver.getPageSource.contains("1 books"))
  }

}

object JpaTest {

  @Deployment def deployment: WebArchive = {
    val descriptor =
      s"""
            | scala:
            | lift:
            |   version: ${LiftWebApp.LIFT_VERSION}
          """.stripMargin

    createWebApp(descriptor, "jpa-default.war")
  }

  private def createWebApp(
      descriptor: String,
      deploymentName: String): WebArchive = {
    val indexHtml =
      <lift:surround with="default" at="content">
        <h2>JEE Examples</h2>
        <p></p>
      </lift:surround>

    LiftWebApp(
      "jpa",
      deploymentName,
      descriptor,
      classOf[JpaBoot],
      List(classOf[Author], classOf[Book], classOf[Model],
        classOf[Authors], classOf[Books]),
      Map(
        "templates-hidden/default.html" -> "",
        "templates-hidden/_sidebar.html" -> "",
        "authors/add.html" -> "",
        "authors/index.html" -> "",
        "books/add.html" -> "",
        "META-INF/persistence.xml" -> "WEB-INF/classes"
      ),
      indexHtml)
  }

}
