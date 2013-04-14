package io.escalante.play.subsystem

import org.jboss.as.server.deployment._
import io.escalante.logging.Log
import io.escalante.artifact.subsystem.ArtifactRepositoryService
import org.jboss.msc.service.ServiceRegistry
import io.escalante.server.Deployments

/**
* // TODO: Document this
* @author Galder Zamarreño
* @since // TODO
*/
class PlayDependencyProcessor extends DeploymentUnitProcessor with Log {

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    for (
      metadata <- PlayDeployment.metadataFromDeployment(deployment)
    ) yield {
      // 1. Install system dependencies...
      val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)
      for (
        (module, artifact) <- metadata.systemDependencies
      ) yield {
        val moduleDependency = module.moduleDependency
        val identifier = moduleDependency.getIdentifier
        trace(s"Add system dependency $identifier to deployment")
        moduleSpec.addSystemDependency(moduleDependency)
      }

      // 2. Attach application jar
      val appJar = metadata.applicationJar.getOrElse {
        throw new DeploymentUnitProcessingException(
          s"Application jar defined in $metadata is missing")
      }
      Deployments.attachJars(deployment, "app-lib", List(appJar))

      // 3. Attach any other extra Play modules the application might require
      Deployments.attachArtifacts(
        deployment, "play-lib", metadata.libraryDependencies)
    }
  }

  def undeploy(context: DeploymentUnit) {}

}
