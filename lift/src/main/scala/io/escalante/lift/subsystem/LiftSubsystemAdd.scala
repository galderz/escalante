/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.dmr.ModelNode
import java.util.List
import org.jboss.msc.service.ServiceController
import org.jboss.as.server.{DeploymentProcessorTarget, AbstractDeploymentChainStep}
import org.jboss.as.server.deployment.Phase._
import io.escalante.logging.Log
import org.jboss.as.controller.{ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import org.jboss.as.controller.services.path.{PathManager, PathManagerService}
import java.util

/**
 * Lift subsystem 'add' operation.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftSubsystemAdd extends AbstractBoottimeAddStepHandler with Log {

  def populateModel(op: ModelNode, model: ModelNode) {
    debug("Populate Lift model")

    // TODO: This is very similar to LiftDescribe impl...
    val relativeToKey = ThirdPartyModulesRepo.RELATIVE_TO
    if (op.hasDefined(relativeToKey))
      model.get(relativeToKey).set(op.get(relativeToKey))

    val pathKey = ThirdPartyModulesRepo.PATH
    if (op.hasDefined(pathKey))
      model.get(pathKey).set(op.get(pathKey))
  }

  override def performBoottime(ctx: OperationContext, op: ModelNode,
    model: ModelNode, verificationHandler: ServiceVerificationHandler,
    newControllers: util.List[ServiceController[_]]) {
    // Add deployment processors here
    ctx.addStep(new AbstractDeploymentChainStep {
      def execute(target: DeploymentProcessorTarget) {
        target.addDeploymentProcessor(
          PARSE, PARSE_WEB_DEPLOYMENT, new LiftParsingProcessor)
        target.addDeploymentProcessor(
          DEPENDENCIES, DEPENDENCIES_WAR_MODULE, new LiftDependencyProcessor)
      }
    }, OperationContext.Stage.RUNTIME)

    val relativeToKey = ThirdPartyModulesRepo.RELATIVE_TO
    val relativeTo =
      if (op.hasDefined(relativeToKey)) op.get(relativeToKey).asString()
      else "jboss.home.dir"

    val pathKey = ThirdPartyModulesRepo.PATH
    val path =
      if (op.hasDefined(pathKey)) op.get(pathKey).asString()
      else "thirdparty-modules"

    val liftService = new LiftService(relativeTo, path)
    val name = LiftService.createServiceName
    val serviceTarget = ctx.getServiceTarget()
    // TODO: It is possible to get the path manager from org.jboss.as.controller.ExtensionContext, see org.jboss.as.server.deployment.scanner.DeploymentScannerExtension
    val controller = serviceTarget
      .addService(name, liftService)
      .addDependency(PathManagerService.SERVICE_NAME,
      classOf[PathManager], liftService.pathManagerInjector)
      .addListener(verificationHandler)
      .setInitialMode(ServiceController.Mode.ACTIVE)
      .install()
    newControllers.add(controller)

    debug("Lift deployment processors added")
  }

}