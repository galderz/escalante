/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.cluster.containervar

import io.escalante.test.lift.LiftWebApp
import io.escalante.test.lift.cluster.NodeUtil._
import io.escalante.test.lift.cluster.containervar.snippet.Words
import org.jboss.arquillian.container.test.api._
import org.jboss.arquillian.junit.{InSequence, Arquillian}
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URL
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.{By, WebDriver}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
@RunAsClient
class ClusterContainerVarTest {

  @ArquillianResource
  var controller: ContainerController = _
  @ArquillianResource
  var deployer: Deployer = _

  val appUrlContainer0 = "http://localhost:8080/cluster-containervar-default"
  val appUrlContainer1 = "http://localhost:8180/cluster-containervar-default"
  val driver = new HtmlUnitDriver()

  @Test @InSequence(-1) // TODO: Ask Rado why manual start is required + check for cluster needed
  def testStartContainers() {
    start(controller, deployer, CONTAINER_0, DEPLOYMENT_0)
    start(controller, deployer, CONTAINER_1, DEPLOYMENT_1)
  }

  @Test @InSequence(1)
  def testStopContainers() {
    stop(controller, deployer, CONTAINER_0, DEPLOYMENT_0)
    stop(controller, deployer, CONTAINER_1, DEPLOYMENT_1)
  }

  @Test
  def testReplicateUpdatedWord() {
    val oldWord = "n/a"
    assertWord(appUrlContainer0, oldWord)
    assertWord(appUrlContainer1, "n/a")
    // Update word in one of the nodes
    updateWord(appUrlContainer0, "Escalante")
    // Verify that the update has been done in both nodes
    assertWord(appUrlContainer0, "Escalante")
    assertWord(appUrlContainer1, "Escalante")
  }

  private def assertWord(url: String, word: String) {
    driver.get(url)
    val source = driver.getPageSource
    assert(source.contains("The current word is: " + word), driver.getPageSource)
  }

  private def updateWord(url: String, word: String) {
    driver.get(url)
    val textBox = driver.findElement(By.cssSelector("input[type='text']"))
    textBox.clear() // Clear the current word first
    textBox.sendKeys(word) // Add new word
    driver.findElement(By.cssSelector("input[type='submit']")).click()
  }

}

object ClusterContainerVarTest {

  @Deployment(name = DEPLOYMENT_0, managed = false)
  @TargetsContainer(CONTAINER_0)
  def deployment0: WebArchive = deployment

  @Deployment(name = DEPLOYMENT_1, managed = false)
  @TargetsContainer(CONTAINER_1)
  def deployment1: WebArchive = deployment

  private def deployment: WebArchive = {
    val descriptor =
      s"""
        | scala:
        | lift:
        |   version: ${LiftWebApp.LIFT_VERSION}
      """.stripMargin
    createWebApp(descriptor, "cluster-containervar-default.war")
  }

  private def createWebApp(
      descriptor: String,
      deploymentName: String): WebArchive = {
    val indexHtml =
      <lift:surround with="default" at="content">
        <h2>The current word is: <lift:words.show /></h2>
        <p>Well, thats just fantastic. See the form below if you want to update this value.</p>

        <h2>Update</h2>
        <form lift="words.update?form=post">
          <p>Word to distribute: <input type="text" /> <input type="submit" /></p>
        </form>
      </lift:surround>

    LiftWebApp(
      "cluster-containervar",
      deploymentName,
      descriptor,
      classOf[ClusterContainerVarBoot],
      List(classOf[Words], classOf[ClusterContainerVarTest]),
      Map("templates-hidden/default.html" -> ""),
      indexHtml,
      replication = true)
  }

}
