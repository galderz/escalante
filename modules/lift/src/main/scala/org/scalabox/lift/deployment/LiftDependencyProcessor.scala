package org.scalabox.lift.deployment

import org.scalabox.lift.extension.{SCALA_291, LIFT_24, LiftMetaData}
import org.jboss.modules.{Module, ModuleIdentifier}
import org.jboss.as.server.deployment.module.ModuleDependency
import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.logging.Log


/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftDependencyProcessor extends DeploymentUnitProcessor {

   import LiftDependencyProcessor._

   def deploy(ctx: DeploymentPhaseContext) {
      val deployment = ctx.getDeploymentUnit
      info("Try to deploy: %s", deployment)
      val liftMetaData = deployment.getAttachment(LiftMetaData.ATTACHMENT_KEY)
      info("Metadata is: %s", liftMetaData)
      val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

      liftMetaData match {
         case LiftMetaData(LIFT_24, SCALA_291) =>
            moduleSpec.addSystemDependency(createDependency(LIFT_24_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(SCALA_291_MODULE_ID))
         case _ => info("Unknown Lift deployment")
      }
   }

   def undeploy(unit: DeploymentUnit) {
      // TODO: Do I need to remove the dependencies?
   }

   private def createDependency(id: ModuleIdentifier): ModuleDependency =
      new ModuleDependency(
            Module.getBootModuleLoader(), id, false, false, false)

}

object LiftDependencyProcessor extends Log {

   // TODO: Add version slot...
   val SCALA_291_MODULE_ID =
      ModuleIdentifier.create("org.scala-lang.scala-library")

   // TODO: Add version slot...
   val LIFT_24_MODULE_ID =
      ModuleIdentifier.create("net.liftweb.lift-mapper_2_9_1")

}