/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.play.subsystem

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.Services

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class PlayInstallProcessor extends DeploymentUnitProcessor with Log {

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    val metaData = PlayMetadata.fromDeployment(deployment).getOrElse {
      return
    }

    val deploymentClassLoader =
      deployment.getAttachment(Attachments.MODULE).getClassLoader
    val playServer = new PlayServerService(metaData.appPath, deploymentClassLoader)
    val serviceName = PlayServerService.getServiceName(metaData.appName)
    val serviceBuilder = ctx.getServiceTarget
        .addService(serviceName, playServer)
        .setInitialMode(ServiceController.Mode.ACTIVE)

    // Add thread pool dependency
    Services.addServerExecutorDependency(
      serviceBuilder, playServer.executorInjector(), false)

    serviceBuilder.install()
  }

  def undeploy(context: DeploymentUnit) {
    // TODO?
  }

}
