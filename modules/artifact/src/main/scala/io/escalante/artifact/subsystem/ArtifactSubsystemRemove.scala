/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact.subsystem

import org.jboss.as.controller.{OperationContext, AbstractRemoveStepHandler}
import org.jboss.dmr.ModelNode

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ArtifactSubsystemRemove extends AbstractRemoveStepHandler {

  override def performRuntime(ctx: OperationContext, op: ModelNode, model: ModelNode) {
    super.performRuntime(ctx, op, model)
    ctx.removeService(ArtifactRepositoryService.SERVICE_NAME)
  }

}
