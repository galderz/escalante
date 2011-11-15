package org.scalabox.lift.deployment

import org.jboss.as.server.deployment.{DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}


/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftVersionProcessor extends DeploymentUnitProcessor {

   def deploy(phaseContext: DeploymentPhaseContext) {

   }

   def undeploy(context: DeploymentUnit) {}

}