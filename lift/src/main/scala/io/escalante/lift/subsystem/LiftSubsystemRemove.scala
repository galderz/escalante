package io.escalante.lift.subsystem

import org.jboss.as.controller.{OperationContext, AbstractRemoveStepHandler}
import org.jboss.dmr.ModelNode

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftSubsystemRemove extends AbstractRemoveStepHandler {

   override def performRuntime(ctx: OperationContext, op: ModelNode, model: ModelNode) {
      super.performRuntime(ctx, op, model)
      ctx.removeService(LiftService.createServiceName)
   }

}
