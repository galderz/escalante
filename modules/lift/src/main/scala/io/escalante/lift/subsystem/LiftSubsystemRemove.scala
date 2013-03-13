/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.controller.{OperationContext, AbstractRemoveStepHandler}
import org.jboss.dmr.ModelNode

/**
 * Lift subsystem 'remove' operation.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftSubsystemRemove extends AbstractRemoveStepHandler {

  override def performRuntime(ctx: OperationContext, op: ModelNode, model: ModelNode) {
    super.performRuntime(ctx, op, model)
    ctx.removeService(LiftService.SERVICE_NAME)
  }

}
