package org.scalabox.lift.extension

import org.jboss.dmr.ModelNode
import org.jboss.as.controller.{PathAddress, OperationContext, OperationStepHandler}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._


/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object Tick extends OperationStepHandler {

   def execute(ctx: OperationContext, op: ModelNode) {
      //Update the model
      val suffix = PathAddress.pathAddress(op.get(ADDRESS)).getLastElement.getValue
      val tick = op.require("value").asLong
      val node = ctx.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel
      node.get("tick").set(tick)

      //Add a step to perform the runtime update
      ctx.addStep(new OperationStepHandler {
         override def execute(ctx: OperationContext, operation: ModelNode) {
            val service = ctx.getServiceRegistry(true)
                    .getRequiredService(LiftService.createServiceName(suffix))
                    .getValue.asInstanceOf[LiftService]
            service.setTick(tick)
            ctx.completeStep
         }
      }, OperationContext.Stage.RUNTIME)

      ctx.completeStep
   }

}