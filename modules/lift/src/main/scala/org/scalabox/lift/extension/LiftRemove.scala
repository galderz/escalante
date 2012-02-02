package org.scalabox.lift.extension

import org.jboss.dmr.ModelNode
import LiftExtension.SUBSYSTEM_NAME
import org.scalabox.logging.Log
import org.jboss.as.controller.{PathAddress, PathElement, OperationContext, AbstractRemoveStepHandler}

import org.jboss.as.controller.descriptions.ModelDescriptionConstants._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftRemove extends AbstractRemoveStepHandler with Log {

   override def performRemove(ctx: OperationContext, op: ModelNode, model: ModelNode) {
      info("Remove the Lift subsystem")
      info("Call super perform remove")
      super.performRemove(ctx, op, model)

      // Remove the subsystem

//      info("Remove resource")
//      ctx.removeResource(PathAddress.pathAddress(op.get(OP_ADDR)))

//      val suffix = PathAddress.pathAddress(op.get(ADDRESS));
//      ctx.removeResource(suffix)

//      ctx.getResourceRegistrationForUpdate.unregisterProxyController(
//            PathElement.pathElement("subsystem", SUBSYSTEM_NAME))
   }

}