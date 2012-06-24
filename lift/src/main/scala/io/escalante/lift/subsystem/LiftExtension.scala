package io.escalante.lift.subsystem

import org.jboss.as.controller.parsing.ExtensionParsingContext
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller.descriptions.DescriptionProvider
import org.jboss.as.controller.descriptions.common.CommonDescriptions
import org.jboss.as.controller._
import io.escalante.logging.Log
import io.escalante.Version
import registry.{Resource, OperationEntry}
import java.util

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftExtension extends Extension {

   import LiftExtension._

   val parser = new LiftSubsystemParser

   def initialize(ctx: ExtensionContext) {
      val subsystem = ctx.registerSubsystem(
            SUBSYSTEM_NAME, Version.MAJOR, Version.MINOR)
      val registry = subsystem.registerSubsystemModel(SUBSYSTEM_DESC)

      // 1. We always need to add an 'add' operation
      registry.registerOperationHandler(ADD,
            LiftSubsystemAdd, SUBSYSTEM_ADD_DESC, false)

      // 2. We always need to add a 'describe' operation
      registry.registerOperationHandler(DESCRIBE,
            LiftDescribeHandler, LiftDescribeHandler, false,
            OperationEntry.EntryType.PRIVATE)

      // 3. We always need to add a 'remove' operation
      registry.registerOperationHandler(REMOVE,
            LiftSubsystemRemove, SUBSYSTEM_REMOVE_DESC, false)

      // Register subsystem XML writer
      subsystem.registerXMLElementWriter(parser)

      info("Lift extension initialized")
   }

   def initializeParsers(ctx: ExtensionParsingContext) {
      ctx.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser)
   }

}

object LiftExtension extends Log {

   val NAMESPACE = "urn:escalante:lift:1.0"

   val SUBSYSTEM_NAME = "lift"

   /**
    * Used to create the description of the subsystem
    */
   val SUBSYSTEM_DESC = new DescriptionProvider() {
      def getModelDescription(locale: util.Locale): ModelNode = {
         // The locale is passed in so you can internationalize the strings
         // used in the descriptions
         val subsystem = new ModelNode()
         subsystem.get(DESCRIPTION).set("This subsystem deploys Lift applications")
         subsystem.get(HEAD_COMMENT_ALLOWED).set(true)
         subsystem.get(TAIL_COMMENT_ALLOWED).set(true)
         subsystem.get(NAMESPACE).set(NAMESPACE)
         subsystem
      }
   }

   val SUBSYSTEM_ADD_DESC = new DescriptionProvider() {
      def getModelDescription(locale: util.Locale): ModelNode = {
         val subsystem = new ModelNode()
         subsystem.get(DESCRIPTION).set("Adds the Lift subsystem")
         subsystem
      }
   }

   val SUBSYSTEM_REMOVE_DESC = new DescriptionProvider() {
      def getModelDescription(locale: util.Locale): ModelNode = {
         val subsystem = new ModelNode()
         subsystem.get(DESCRIPTION).set("Removes the Lift subsystem")
         subsystem
      }
   }

   def createAddSubsystemOperation: ModelNode = {
      val subsystem = new ModelNode
      subsystem.get(OP).set(ADD)
      subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME)
      subsystem
   }

}

private object LiftDescribeHandler extends OperationStepHandler with DescriptionProvider {

   import LiftExtension._

   def getModelDescription(locale: util.Locale) =
      CommonDescriptions.getSubsystemDescribeOperation(locale)

   def execute(context: OperationContext, operation: ModelNode) {
      debug("Describe lift extension")

      val addOp = createAddSubsystemOperation
      val model = Resource.Tools.readModel(context.readResource(PathAddress.EMPTY_ADDRESS))

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