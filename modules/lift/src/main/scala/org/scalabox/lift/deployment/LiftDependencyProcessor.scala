package org.scalabox.lift.deployment

import org.jboss.as.server.deployment.{DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.jboss.modules.ModuleIdentifier


/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftDependencyProcessor extends DeploymentUnitProcessor {

   def deploy(ctx: DeploymentPhaseContext) {


   }

   def undeploy(unit: DeploymentUnit) {

   }

}

object LiftDependencyProcessor {

   val SCALA_291 = ModuleIdentifier.create("org.scala-lang.scala-library", "2.9.1")

}