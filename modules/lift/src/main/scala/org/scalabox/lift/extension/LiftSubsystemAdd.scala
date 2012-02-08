package org.scalabox.lift.extension

import org.jboss.dmr.ModelNode
import java.util.List
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.{DeploymentProcessorTarget, AbstractDeploymentChainStep}
import org.jboss.as.server.deployment.Phase._
import org.scalabox.logging.Log
import org.jboss.as.controller.{AbstractAddStepHandler, ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import org.scalabox.lift.deployment.{LiftDeploymentProcessor, LiftParsingProcessor, LiftDependencyProcessor}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftSubsystemAdd extends AbstractBoottimeAddStepHandler with Log {

   def populateModel(operation: ModelNode, model: ModelNode) {
      info("Populate Lift model")
      model.setEmptyObject
   }

   override def performBoottime(ctx: OperationContext, op: ModelNode,
            model: ModelNode, verificationHandler: ServiceVerificationHandler,
            newControllers: List[ServiceController[_]]) {
      // Add deployment processors here
      ctx.addStep(new AbstractDeploymentChainStep {
         def execute(target: DeploymentProcessorTarget) {
            target.addDeploymentProcessor(
               PARSE, PARSE_WEB_DEPLOYMENT, new LiftParsingProcessor)
            target.addDeploymentProcessor(
               DEPENDENCIES, DEPENDENCIES_WAR_MODULE, new LiftDependencyProcessor)
            target.addDeploymentProcessor(
               INSTALL, INSTALL_WAR_DEPLOYMENT, new LiftDeploymentProcessor)
         }
      }, OperationContext.Stage.RUNTIME)

      info("Lift deployment processors added")
   }

}