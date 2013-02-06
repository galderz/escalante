/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact.subsystem

import org.jboss.as.controller.{ServiceVerificationHandler, OperationContext, AbstractBoottimeAddStepHandler}
import io.escalante.logging.Log
import org.jboss.dmr.ModelNode
import org.jboss.msc.service.ServiceController
import org.jboss.as.controller.services.path.{PathManager, PathManagerService}
import java.util

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ArtifactSubsystemAdd extends AbstractBoottimeAddStepHandler with Log {

  def populateModel(op: ModelNode, model: ModelNode) {
    debug("Populate Lift model")

    // TODO: This is very similar to ArtifactDescribe impl...
    val relativeToKey = ThirdPartyModulesRepo.RELATIVE_TO
    if (op.hasDefined(relativeToKey))
      model.get(relativeToKey).set(op.get(relativeToKey))

    val pathKey = ThirdPartyModulesRepo.PATH
    if (op.hasDefined(pathKey))
      model.get(pathKey).set(op.get(pathKey))
  }

  override def performBoottime(
      ctx: OperationContext,
      op: ModelNode,
      model: ModelNode,
      verificationHandler: ServiceVerificationHandler,
      newControllers: util.List[ServiceController[_]]) {
    val pathKey = ThirdPartyModulesRepo.PATH
    val pathDefined = op.hasDefined(pathKey)
    val path =
      if (pathDefined) ctx.resolveExpressions(op.get(pathKey)).asString()
      else "thirdparty-modules"

    val relativeToKey = ThirdPartyModulesRepo.RELATIVE_TO
    val relativeTo =
      if (op.hasDefined(relativeToKey)) Some(op.get(relativeToKey).asString())
      else if (!pathDefined) Some("jboss.home.dir")
      else None

    val service = new ArtifactRepositoryService(relativeTo, path)
    val name = ArtifactRepositoryService.SERVICE_NAME
    val serviceTarget = ctx.getServiceTarget
    // TODO: It is possible to get the path manager from org.jboss.as.controller.ExtensionContext, see org.jboss.as.server.deployment.scanner.DeploymentScannerExtension
    val controller = serviceTarget
        .addService(name, service)
        .addDependency(PathManagerService.SERVICE_NAME,
      classOf[PathManager], service.pathManagerInjector)
        .addListener(verificationHandler)
        .setInitialMode(ServiceController.Mode.ACTIVE)
        .install()
    newControllers.add(controller)

    debug("Artifact service created and installed")
  }

}
