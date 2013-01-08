/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.jpa

import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive
import io.escalante.lift.AbstractLiftWebAppTest
import snippet.{Books, Authors}
import scala.xml.Elem
import org.junit.Test
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.By

/**
 * Library JPA test
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class LibraryJpaTest {

  protected def appUrl: String = "http://localhost:8080/libraryjpa-282"

  @Test def testAddAuthorAndBook() {
    val driver = new HtmlUnitDriver()

    // 1. Go to index.html and check it says: "JEE Examples"
    driver.get(appUrl)
    assert(driver.getPageSource.contains("JEE Examples"))

    // 2. Add an author
    driver.get("%s/authors/add".format(appUrl))
    driver.findElement(By.cssSelector("input[type='text']")).sendKeys("Galder")
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // 3. Check if author list contains the added author
    driver.get("%s/authors/index".format(appUrl))
    assert(driver.getPageSource.contains("Galder"))
    assert(driver.getPageSource.contains("0 books"))

    // 4. Add a new Book with the added author
    driver.get("%s/books/add".format(appUrl))
    // First text input is Title
    driver.findElement(By.cssSelector("input[type='text']")).sendKeys("Escalante In Action")
    driver.findElement(By.cssSelector("input[type='submit']")).click()

    // 5. Check if the books for the author have been updated
    driver.get("%s/authors".format(appUrl))
    assert(driver.getPageSource.contains("Galder"))
    assert(driver.getPageSource.contains("1 books"))
  }

}

object LibraryJpaTest extends AbstractLiftWebAppTest {

  @Deployment def deployment: WebArchive = {
    deployment("2.8.2", "2.4", classOf[LibraryJpaBoot], List())
  }

  def deployment(scala: String, lift: String, bootClass: Class[_ <: AnyRef],
        classes: Seq[Class[_]]): WebArchive = {
    deployment("libraryjpa", "libraryjpa-%s.war".format(scala.replace(".", "")),
      descriptor(scala, lift), bootClass,
      List(classOf[Author], classOf[Book], classOf[Model],
        classOf[Authors], classOf[Books]) ++ classes)
  }

  def descriptor(scala: String, lift: String): String =
    """
      | scala:
      |   version: %s
      | lift:
      |   version: %s
      |   modules:
      |     - mapper
    """.format(scala, lift).stripMargin

  override val indexHtml: Elem =
    <lift:surround with="default" at="content">
      <h2>JEE Examples</h2>
      <p></p>
    </lift:surround>

  override val webResources: Map[String, String] = Map(
    "templates-hidden/default.html" -> "",
    "templates-hidden/_sidebar.html" -> "",
    "authors/add.html" -> "",
    "authors/index.html" -> "",
    "books/add.html" -> "",
    "META-INF/persistence.xml" -> "WEB-INF/classes"
  )

  override val static: Option[Elem] = None

}
