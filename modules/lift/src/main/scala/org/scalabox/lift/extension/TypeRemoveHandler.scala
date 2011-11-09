package org.scalabox.lift.extension

import org.jboss.as.controller.descriptions.DescriptionProvider
import java.util.Locale
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller.{OperationContext, PathAddress, AbstractRemoveStepHandler}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object TypeRemoveHandler extends AbstractRemoveStepHandler with DescriptionProvider {

   def getModelDescription(locale: Locale): ModelNode = {
      val node = new ModelNode
      node.get(DESCRIPTION).set("Removes a Lift deployment type")
      node
   }

   protected override def performRuntime(ctx: OperationContext,
            op: ModelNode, model: ModelNode){
      val suffix = PathAddress.pathAddress(op.get(ADDRESS)).getLastElement.getValue
      val name = LiftService.createServiceName(suffix)
      ctx.removeService(name)
   }

}