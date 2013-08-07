/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift.cluster.cometchat

import io.escalante.test.lift.LiftWebApp
import io.escalante.test.lift.cluster.NodeUtil._
import io.escalante.test.lift.cluster.cometchat.actors._
import io.escalante.test.lift.cluster.cometchat.service._
import io.escalante.test.lift.cluster.cometchat.snippet.AddChatComet
import org.jboss.arquillian.container.test.api._
import org.jboss.arquillian.junit.{InSequence, Arquillian}
import org.jboss.arquillian.test.api.ArquillianResource
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.junit.Test
import org.junit.runner.RunWith
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import io.escalante.test.lift.cluster.cometchat.service.CentralChatServer.ChatMessages

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
@RunAsClient
class ClusterCometChatTest {

  @ArquillianResource
  var controller: ContainerController = _
  @ArquillianResource
  var deployer: Deployer = _

  val appUrlContainer0 = "http://localhost:8080/cluster-cometchat-default"
  val appUrlContainer1 = "http://localhost:8180/cluster-cometchat-default"
  val driver = new HtmlUnitDriver()

  @Test @InSequence(-1) // TODO: Ask Rado why manual start i  s required + check for cluster needed
  def testStartContainers() {
    start(controller, deployer, CONTAINER_0, DEPLOYMENT_0)
//    start(controller, deployer, CONTAINER_1, DEPLOYMENT_1)
  }

  @Test @InSequence(1)
  def testStopContainers() {
    stop(controller, deployer, CONTAINER_0, DEPLOYMENT_0)
//    stop(controller, deployer, CONTAINER_1, DEPLOYMENT_1)
  }

  @Test
  def testReplicateChatMessages() {
    // TODO
    while(true) {
      Thread.sleep(1000);
    }
  }

}

object ClusterCometChatTest {

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
    createWebApp(descriptor, "cluster-cometchat-default.war")
  }

  private def createWebApp(
      descriptor: String,
      deploymentName: String): WebArchive = {
    val indexHtml =
      <div id="main" class="lift:surround?with=default;at=content">
        <h2>Welcome to our distributed chat server!</h2>
        <div data-lift="form.ajax">
          <div data-lift="AddChatComet">
            <span id="hostname">This server's hostname is </span><br/>
            <strong>Messages</strong>
            <ul id="messages">
              <li></li>
            </ul>
            <hr/>
              <form class="form-horizontal">
                <fieldset>
                  <div class="control-group">
                    <input id="nickname" type="text" class="input-medium" placeholder="Nickname" />
                  </div>
                  <div class="control-group">
                    <input type="text" class="input-xlarge" id="message" />
                  </div>
                  <div class="control-group">
                    <button type="submit" class="btn">Chat!</button>
                  </div>
                </fieldset>
              </form>
            <hr/>
          </div>
        </div>
      </div>

      LiftWebApp(
        "cluster-cometchat",
        deploymentName,
        descriptor,
        classOf[ClusterCometChatBoot],
        List(
          "cluster.cometchat", "cluster.cometchat.actor",
          "cluster.cometchat.service", "cluster.cometchat.snippet",
          "cluster.cometchat.comet"
        ),
        Map(
          "templates-hidden/default.html" -> "",
          "js" -> "",
          "bootstrap" -> ""
        ),
        indexHtml,
        replication = true)
  }

}
