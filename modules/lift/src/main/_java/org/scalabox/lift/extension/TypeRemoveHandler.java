package org.scalabox.lift.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceName;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.Locale;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class TypeRemoveHandler extends AbstractRemoveStepHandler
      implements DescriptionProvider {

   public static final TypeRemoveHandler INSTANCE = new TypeRemoveHandler();

   private TypeRemoveHandler() {
   }

   @Override
   public ModelNode getModelDescription(Locale locale) {
      ModelNode node = new ModelNode();
      node.get(DESCRIPTION).set("Removes a Lift deployment type");
      return node;
   }

   @Override
   protected void performRuntime(OperationContext context,
               ModelNode operation, ModelNode model)
         throws OperationFailedException {
      String suffix = PathAddress.pathAddress(
            operation.get(ADDRESS)).getLastElement().getValue();
      ServiceName name = LiftService.createServiceName(suffix);
      context.removeService(name);
   }

}
