/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log
import org.jboss.msc.service.ServiceRegistry
import io.escalante.server.{Deployments, JBossModule}

/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftDependencyProcessor extends DeploymentUnitProcessor with Log {

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    trace("Try to deploy: %s", deployment)
    for (
      liftMetadata <- LiftMetadata.fromDeployment(deployment)
    ) yield {
      debug("Metadata is: %s", liftMetadata)

      val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

      val liftService = getLiftService(ctx.getServiceRegistry)

      // Install Scala dependencies according to the version required.
      // If deployment uses same Scala version as this subsystem, just link it
      // Otherwise, resolve and install the desired Scala version
      val module =
        liftMetadata.scalaVersion match {
          case s if s.isMain => JBossModule(s)
          case _ => liftService.installScalaModule(liftMetadata.scalaVersion)
        }
      moduleSpec.addSystemDependency(module.moduleDependency)

      // Install other system dependencies...
      for ((module, artifact) <- liftMetadata.systemDependencies)
      yield moduleSpec.addSystemDependency(module.moduleDependency)

      // Hack Lift jars into the war file cos Lift framework is currently
      // designed to be solely used by one web-app in (class loader) isolation
      Deployments.attachArtifacts(
        deployment, "WEB-INF/lift-lib", liftMetadata.libraryDependencies)
    }
  }

  def undeploy(unit: DeploymentUnit) {
    // No-op
  }

  private def getLiftService(registry: ServiceRegistry): LiftService = {
    val container = registry.getService(LiftService.SERVICE_NAME)
    container.getValue.asInstanceOf[LiftService]
  }

}
