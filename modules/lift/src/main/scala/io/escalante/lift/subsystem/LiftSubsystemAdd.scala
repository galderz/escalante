/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.dmr.ModelNode
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.{DeploymentProcessorTarget, AbstractDeploymentChainStep}
import org.jboss.as.server.deployment.Phase._
import io.escalante.logging.Log
import org.jboss.as.controller.{ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import java.util
import io.escalante.artifact.subsystem.ArtifactRepositoryService
import io.escalante.artifact.ArtifactRepository

/**
 * Lift subsystem 'add' operation.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftSubsystemAdd extends AbstractBoottimeAddStepHandler with Log {

  def populateModel(op: ModelNode, model: ModelNode) {
    debug("Populate Lift model")
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
        target.addDeploymentProcessor(LiftExtension.LIFT_SUBSYSTEM_NAME,
          PARSE, PARSE_WEB_DEPLOYMENT, new LiftParsingProcessor)
        target.addDeploymentProcessor(LiftExtension.LIFT_SUBSYSTEM_NAME,
          DEPENDENCIES, DEPENDENCIES_WAR_MODULE, new LiftDependencyProcessor)
      }
    }, OperationContext.Stage.RUNTIME)

    val liftService = new LiftService()
    val name = LiftService.SERVICE_NAME
    val serviceTarget = ctx.getServiceTarget
    val controller = serviceTarget
      .addService(name, liftService)
      .addDependency(ArtifactRepositoryService.SERVICE_NAME,
          classOf[ArtifactRepository], liftService.artifactRepositoryInjector)
      .addListener(verificationHandler)
      .setInitialMode(ServiceController.Mode.ACTIVE)
      .install()
    newControllers.add(controller)

    debug("Lift deployment processors added")
  }

}