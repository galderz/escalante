/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.play.subsystem

import org.jboss.dmr.ModelNode
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.{DeploymentProcessorTarget, AbstractDeploymentChainStep}
import org.jboss.as.server.deployment.Phase._
import io.escalante.logging.Log
import org.jboss.as.controller.{ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import java.util

/**
 * Play subsystem 'add' operation.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object PlaySubsystemAdd extends AbstractBoottimeAddStepHandler with Log {

  def populateModel(op: ModelNode, model: ModelNode) {
    debug("Populate Play model")
  }

  override def performBoottime(
      ctx: OperationContext,
      op: ModelNode,
      model: ModelNode,
      verificationHandler: ServiceVerificationHandler,
      newControllers: util.List[ServiceController[_]]) {
    // Add deployment processors here
    ctx.addStep(new AbstractDeploymentChainStep {
      def execute(target: DeploymentProcessorTarget) {
        // Before STRUCTURE_MOUNT
        target.addDeploymentProcessor(PlayExtension.PLAY_SUBSYSTEM_NAME,
          STRUCTURE, 0x0125, new PlayRootMountProcessor)
        target.addDeploymentProcessor(PlayExtension.PLAY_SUBSYSTEM_NAME,
          PARSE, 0x0075, new PlayParsingProcessor)
        target.addDeploymentProcessor(PlayExtension.PLAY_SUBSYSTEM_NAME,
          DEPENDENCIES, 0x0175, new PlayDependencyProcessor)
        target.addDeploymentProcessor(PlayExtension.PLAY_SUBSYSTEM_NAME,
          INSTALL, 0x0275, new PlayInstallProcessor)
      }
    }, OperationContext.Stage.RUNTIME)


    info("Start Escalante Play subsystem")
  }

}