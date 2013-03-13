/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact.subsystem

import org.jboss.as.controller._
import org.jboss.as.controller.parsing.ExtensionParsingContext
import io.escalante.logging.Log
import org.jboss.as.controller.descriptions.DescriptionProvider
import java.util
import org.jboss.dmr.{ModelType, ModelNode}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import registry.{OperationEntry, Resource}
import io.escalante.Version

/**
 * Artifact extension.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class ArtifactExtension extends Extension {

  import ArtifactExtension._

  val parser = new ArtifactSubsystemParser

  def initialize(ctx: ExtensionContext) {
    val subsystem = ctx.registerSubsystem(
      ARTIFACT_SUBSYSTEM_NAME, Version.MAJOR, Version.MINOR)
    val registry = subsystem.registerSubsystemModel(ARTIFACT_SUBSYSTEM_DESC)

    // 1. We always need to add an 'add' operation
    registry.registerOperationHandler(ADD,
      ArtifactSubsystemAdd, ARTIFACT_SUBSYSTEM_ADD_DESC, false)

    // 2. We always need to add a 'describe' operation
    registry.registerOperationHandler(DESCRIBE,
      ArtifactDescribeHandler, ArtifactDescribeHandler, false,
      OperationEntry.EntryType.PRIVATE)

    // 3. We always need to add a 'remove' operation
    registry.registerOperationHandler(REMOVE,
      ArtifactSubsystemRemove, ARTIFACT_SUBSYSTEM_REMOVE_DESC, false)

    // Register subsystem XML writer
    subsystem.registerXMLElementWriter(parser)

  }

  def initializeParsers(ctx: ExtensionParsingContext) {
    ctx.setSubsystemXmlMapping(
      ARTIFACT_SUBSYSTEM_NAME, ARTIFACT_SUBSYSTEM_NAMESPACE, parser)
  }

}

object ArtifactExtension extends Log {

  val ARTIFACT_SUBSYSTEM_NAMESPACE = "urn:escalante:artifact:1.0"

  val ARTIFACT_SUBSYSTEM_NAME = "artifact"

  /**
   * Used to create the description of the subsystem
   */
  val ARTIFACT_SUBSYSTEM_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      val subsystem = new ModelNode()
      subsystem.get(DESCRIPTION).set(
        "This subsystem resolves, installs and attaches Artifacts")
      subsystem.get(HEAD_COMMENT_ALLOWED).set(true)
      subsystem.get(TAIL_COMMENT_ALLOWED).set(true)
      subsystem.get(NAMESPACE).set(ARTIFACT_SUBSYSTEM_NAMESPACE)
      subsystem
    }
  }

  val ARTIFACT_SUBSYSTEM_ADD_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      val desc = new ModelNode()
      desc.get(OPERATION_NAME).set(ADD)
      desc.get(DESCRIPTION).set("Adds the Artifact subsystem")
      desc.get(REQUEST_PROPERTIES, ThirdPartyModulesRepo.PATH, DESCRIPTION)
          .set("Thirdparty modules repository path")
      desc.get(REQUEST_PROPERTIES, ThirdPartyModulesRepo.PATH, TYPE)
          .set(ModelType.EXPRESSION)
      // If absent, a default path is used
      desc.get(REQUEST_PROPERTIES, ThirdPartyModulesRepo.PATH, REQUIRED)
          .set(false)
      desc
    }
  }

  val ARTIFACT_SUBSYSTEM_REMOVE_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      val subsystem = new ModelNode()
      subsystem.get(OPERATION_NAME).set(REMOVE)
      subsystem.get(DESCRIPTION).set("Removes the Lift subsystem")
      subsystem
    }
  }

  def createAddSubsystemOperation: ModelNode = {
    val subsystem = new ModelNode
    subsystem.get(OP).set(ADD)
    subsystem.get(OP_ADDR).add(SUBSYSTEM, ARTIFACT_SUBSYSTEM_NAME)
    subsystem
  }

}

private object ArtifactDescribeHandler
    extends OperationStepHandler with DescriptionProvider {

  import ArtifactExtension._

  def getModelDescription(locale: util.Locale) =
    new ModelNode

  def execute(context: OperationContext, operation: ModelNode) {
    debug("Describe Artifact extension")

    val addOp = createAddSubsystemOperation
    val model = Resource.Tools.readModel(context
        .readResource(PathAddress.EMPTY_ADDRESS))

    val pathKey = ThirdPartyModulesRepo.PATH
    if (model.hasDefined(pathKey))
      addOp.get(pathKey).set(model.get(pathKey))

    val relativeToKey = ThirdPartyModulesRepo.RELATIVE_TO
    if (model.hasDefined(relativeToKey))
      addOp.get(relativeToKey).set(model.get(relativeToKey))

    // Add the main operation
    context.getResult.add(addOp)
    context.completeStep
  }

}