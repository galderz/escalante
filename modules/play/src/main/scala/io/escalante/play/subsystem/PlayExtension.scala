package io.escalante.play.subsystem

import org.jboss.as.controller.Extension
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller.descriptions.DescriptionProvider
import org.jboss.as.controller._
import io.escalante.logging.Log
import java.util
import org.jboss.dmr.ModelNode
import parsing.ExtensionParsingContext
import registry.{OperationEntry, Resource}
import io.escalante.Version

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class PlayExtension extends Extension {

  import PlayExtension._

  val parser = new PlaySubsystemParser

  def initialize(ctx: ExtensionContext) {
    val subsystem = ctx.registerSubsystem(
      PLAY_SUBSYSTEM_NAME, Version.MAJOR, Version.MINOR)
    val registry = subsystem.registerSubsystemModel(SUBSYSTEM_DESC)

    // 1. We always need to add an 'add' operation
    registry.registerOperationHandler(ADD,
      PlaySubsystemAdd, SUBSYSTEM_ADD_DESC, false)

    // 2. We always need to add a 'describe' operation
    registry.registerOperationHandler(DESCRIBE,
      PlayDescribeHandler, PlayDescribeHandler, false,
      OperationEntry.EntryType.PRIVATE)

    // 3. We always need to add a 'remove' operation
    registry.registerOperationHandler(REMOVE,
      PlaySubsystemRemove, SUBSYSTEM_REMOVE_DESC, false)

    // Register subsystem XML writer
    subsystem.registerXMLElementWriter(parser)
  }

  def initializeParsers(ctx: ExtensionParsingContext) {
    ctx.setSubsystemXmlMapping(
      PLAY_SUBSYSTEM_NAME, PLAY_SUBSYSTEM_NAMESPACE, parser)
  }

}

object PlayExtension extends Log {

  val PLAY_SUBSYSTEM_NAMESPACE = "urn:escalante:play:1.0"

  val PLAY_SUBSYSTEM_NAME = "play"

  /**
   * Used to create the description of the subsystem
   */
  val SUBSYSTEM_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      // The locale is passed in so you can internationalize the strings
      // used in the descriptions
      val subsystem = new ModelNode()
      subsystem.get(DESCRIPTION).set("This subsystem deploys Play applications")
      subsystem.get(HEAD_COMMENT_ALLOWED).set(true)
      subsystem.get(TAIL_COMMENT_ALLOWED).set(true)
      subsystem.get(NAMESPACE).set(PLAY_SUBSYSTEM_NAMESPACE)
      subsystem
    }
  }

  val SUBSYSTEM_ADD_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      val desc = new ModelNode()
      desc.get(OPERATION_NAME).set(ADD)
      desc.get(DESCRIPTION).set("Adds the Play subsystem")
      desc
    }
  }

  val SUBSYSTEM_REMOVE_DESC = new DescriptionProvider() {
    def getModelDescription(locale: util.Locale): ModelNode = {
      val subsystem = new ModelNode()
      subsystem.get(OPERATION_NAME).set(REMOVE)
      subsystem.get(DESCRIPTION).set("Removes the Play subsystem")
      subsystem
    }
  }

  def createAddSubsystemOperation: ModelNode = {
    val subsystem = new ModelNode
    subsystem.get(OP).set(ADD)
    subsystem.get(OP_ADDR).add(SUBSYSTEM, PLAY_SUBSYSTEM_NAME)
    subsystem
  }

}

private object PlayDescribeHandler
    extends OperationStepHandler with DescriptionProvider {

  import PlayExtension._

  def getModelDescription(locale: util.Locale) =
    new ModelNode

  def execute(context: OperationContext, operation: ModelNode) {
    debug("Describe Play extension")

    val addOp = createAddSubsystemOperation
    Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS))

    // Add the main operation
    context.getResult.add(addOp)
    context.completeStep
  }

}