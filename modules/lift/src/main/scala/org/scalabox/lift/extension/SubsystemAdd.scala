package org.scalabox.lift.extension

import org.jboss.dmr.ModelNode
import org.jboss.as.controller.{ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import java.util.List
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.{DeploymentProcessorTarget, AbstractDeploymentChainStep}
import org.scalabox.lift.deployment.SubsystemDeployment

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object SubsystemAdd extends AbstractBoottimeAddStepHandler {

   def performBoottime(context: OperationContext, operation: ModelNode,
            model: ModelNode, verificationHandler: ServiceVerificationHandler,
            newControllers: List[ServiceController[_]]) {
      // Add deployment processors here
      // Remove this if you don't need to hook into the deployers, or you can add as many as you like
      // see SubDeploymentProcessor for explanation of the phases

      context.addStep(new AbstractDeploymentChainStep {
         def execute(processorTarget: DeploymentProcessorTarget): Unit = {
            processorTarget.addDeploymentProcessor(
               SubsystemDeployment.PHASE,
               SubsystemDeployment.PRIORITY,
               new SubsystemDeployment)
         }
      }, OperationContext.Stage.RUNTIME)
   }

   def populateModel(operation: ModelNode, model: ModelNode) {
      // Initialise the 'type' child node
      model.get("type").setEmptyObject()
   }

}