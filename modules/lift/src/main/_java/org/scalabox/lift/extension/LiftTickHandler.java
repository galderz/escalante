package org.scalabox.lift.extension;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import org.jboss.dmr.ModelNode;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * // TODO: Document this
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
public class LiftTickHandler implements OperationStepHandler {

   public static final LiftTickHandler INSTANCE = new LiftTickHandler();

   private LiftTickHandler() {
      // Singleton
   }

   @Override
   public void execute(OperationContext ctx, ModelNode op)
         throws OperationFailedException {
      //Update the model
      final String suffix = PathAddress.pathAddress(op.get(ADDRESS))
            .getLastElement().getValue();
      final long tick = op.require("value").asLong();
      ModelNode node = ctx.readResourceForUpdate(PathAddress.EMPTY_ADDRESS).getModel();
      node.get("tick").set(tick);

      //Add a step to perform the runtime update
      ctx.addStep(new OperationStepHandler() {
         @Override
         public void execute(OperationContext ctx, ModelNode operation)
               throws OperationFailedException {
            LiftService service = (LiftService)
                  ctx.getServiceRegistry(true).getRequiredService(
                        LiftService.createServiceName(suffix)).getValue();
            service.setTick(tick);
            ctx.completeStep();
         }
      }, OperationContext.Stage.RUNTIME);

      ctx.completeStep();
   }

}
