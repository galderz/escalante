package io.escalante.play.subsystem

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log

/**
* // TODO: Document this
* @author Galder Zamarreño
* @since // TODO
*/
class PlayDependencyProcessor extends DeploymentUnitProcessor with Log {

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    val metaData = PlayMetadata.fromDeployment(deployment).getOrElse {
      return
    }

    // Install system dependencies...
    val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)
    for (module <- metaData.systemDependencies)
    yield {
      val moduleDependency = module.moduleDependency
      val identifier = moduleDependency.getIdentifier
      trace(s"Add system dependency $identifier to deployment")
      moduleSpec.addSystemDependency(moduleDependency)
    }
  }

  def undeploy(context: DeploymentUnit) {}

}
