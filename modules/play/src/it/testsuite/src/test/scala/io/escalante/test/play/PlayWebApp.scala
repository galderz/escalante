/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play

import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentHelper
import org.jboss.as.controller.client.ModelControllerClient

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class PlayWebApp(appName: String) {

  import PlayWebApp._

  val deployName = s"$appName.yml"

  def deploy(withDb: Boolean) {
    val descriptor = new PlayDescriptor(deployName, appName, withDb)

    // Package static Play app (reloadable apps will be supported in future)
    PlayConsole.packageApp(descriptor.appPath)

    ServerClient.deploy(deployName, descriptor.exportAsStream)
  }

  def undeploy() {
    ServerClient.undeploy(deployName)
  }

}

object PlayWebApp {

  val ServerClient = new ServerDeploymentHelper(
    ModelControllerClient.Factory.create("localhost", 9999))

}
