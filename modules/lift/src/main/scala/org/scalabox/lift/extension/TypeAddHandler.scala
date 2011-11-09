package org.scalabox.lift.extension

import org.jboss.as.controller.descriptions.DescriptionProvider
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.{ModelType, ModelNode}
import java.util.Locale
import org.jboss.as.controller.{PathAddress, ServiceVerificationHandler, OperationContext, AbstractAddStepHandler}
import org.jboss.msc.service.ServiceController

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object TypeAddHandler extends AbstractAddStepHandler with DescriptionProvider {

   def populateModel(op: ModelNode, model: ModelNode) {
      // The default value is 10000 if it has not been defined
      val tick =
         if (op.hasDefined("tick"))
            op.get("tick").asLong   // Read value from the operation
         else 10000

      model.get("tick").set(tick)
   }

   def getModelDescription(locale: Locale): ModelNode = {
      val node = new ModelNode();
      node.get(DESCRIPTION).set("Adds a Lift deployment type");
      node.get(REQUEST_PROPERTIES, "tick", DESCRIPTION).set("How often to output information about a tracked deployment");
      node.get(REQUEST_PROPERTIES, "tick", TYPE).set(ModelType.LONG);
      node.get(REQUEST_PROPERTIES, "tick", REQUIRED).set(false);
      node.get(REQUEST_PROPERTIES, "tick", DEFAULT).set(10000);
      node;
   }

   override def performRuntime(ctx: OperationContext, op: ModelNode,
            model: ModelNode, verificationHandler: ServiceVerificationHandler,
            newControllers: java.util.List[ServiceController[_]]) {
      val suffix = PathAddress.pathAddress(op.get(ADDRESS)).getLastElement.getValue
      val service = new LiftService(suffix, model.get("tick").asLong)
      val name = LiftService.createServiceName(suffix)
      val controller = ctx.getServiceTarget
              .addService(name, service)
              .addListener(verificationHandler)
              .setInitialMode(ServiceController.Mode.ACTIVE)
              .install
      newControllers.add(controller)
   }

}