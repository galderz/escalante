package org.scalabox.lift.extension;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.List;
import java.util.Locale;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class TypeAddHandler extends AbstractAddStepHandler
      implements DescriptionProvider {

   public static final TypeAddHandler INSTANCE = new TypeAddHandler();

   private TypeAddHandler() {
      // Singleton
   }

   @Override
   protected void populateModel(ModelNode op, ModelNode model)
         throws OperationFailedException {
      // The default value is 10000 if it has not been defined
      long tick = 10000;
      // Read value from the operation
      if (op.hasDefined("tick"))
         tick = op.get("tick").asLong();

      model.get("tick").set(tick);
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      ModelNode node = new ModelNode();
      node.get(DESCRIPTION).set("Adds a Lift deployment type");
      node.get(REQUEST_PROPERTIES, "tick", DESCRIPTION).set("How often to output information about a tracked deployment");
      node.get(REQUEST_PROPERTIES, "tick", TYPE).set(ModelType.LONG);
      node.get(REQUEST_PROPERTIES, "tick", REQUIRED).set(false);
      node.get(REQUEST_PROPERTIES, "tick", DEFAULT).set(10000);
      return node;
   }

   @Override
   protected void performRuntime(OperationContext context, ModelNode operation,
               ModelNode model, ServiceVerificationHandler verificationHandler,
               List<ServiceController<?>> newControllers)
         throws OperationFailedException {
      String suffix = PathAddress.pathAddress(operation.get(ADDRESS))
            .getLastElement().getValue();
      LiftService service = new LiftService(suffix, model.get("tick").asLong());
      ServiceName name = LiftService.createServiceName(suffix);
      ServiceController<LiftService> controller = context.getServiceTarget()
         .addService(name, service)
         .addListener(verificationHandler)
         .setInitialMode(ServiceController.Mode.ACTIVE)
         .install();
      newControllers.add(controller);
   }

}
