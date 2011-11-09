package org.scalabox.lift.extension

import javax.xml.stream.XMLStreamConstants._
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.persistence.SubsystemMarshallingContext
import org.jboss.staxmapper.{XMLExtendedStreamWriter, XMLExtendedStreamReader, XMLElementWriter, XMLElementReader}
import org.jboss.as.controller.parsing.{ParseUtils, ExtensionParsingContext}
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.as.controller._
import descriptions.common.CommonDescriptions
import descriptions.DescriptionProvider
import java.util.{Locale, Collections}
import registry.{AttributeAccess, OperationEntry, ManagementResourceRegistration}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class SubsystemExtension extends Extension {

   import SubsystemExtension._

   val parser = new SubsystemParser

   def initialize(context: ExtensionContext) {
      val subsystem = context.registerSubsystem(SUBSYSTEM_NAME)
      val registration = subsystem.registerSubsystemModel(SubsystemProviders.SUBSYSTEM)

      // We always need to add an 'add' operation
      registration.registerOperationHandler(ADD,
            SubsystemAdd, SubsystemProviders.SUBSYSTEM_ADD, false)

      // We always need to add a 'describe' operation
      registration.registerOperationHandler(DESCRIBE,
            SubsystemDescribeHandler, SubsystemDescribeHandler, false,
            OperationEntry.EntryType.PRIVATE)

      //Add the type child
      val typeChild = registration.registerSubModel(PathElement.pathElement("type"), SubsystemProviders.TYPE_CHILD)
      typeChild.registerOperationHandler(ADD, TypeAddHandler, TypeAddHandler)
      typeChild.registerOperationHandler(REMOVE, TypeRemoveHandler, TypeRemoveHandler)
      typeChild.registerReadWriteAttribute("tick", null, Tick, AttributeAccess.Storage.CONFIGURATION)
      subsystem.registerXMLElementWriter(parser)
   }

   def initializeParsers(ctx: ExtensionParsingContext) =
      ctx.setSubsystemXmlMapping(NAMESPACE, parser)

}

object SubsystemExtension {

   val NAMESPACE = "urn:org.scalabox:lift:1.0"

   val SUBSYSTEM_NAME = "lift"

   def createAddSubsystemOperation: ModelNode = {
      val subsystem: ModelNode = new ModelNode
      subsystem.get(OP).set(ADD)
      subsystem.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_NAME)
      return subsystem
   }

}

class SubsystemParser extends XMLElementReader[java.util.List[ModelNode]]
      with XMLElementWriter[SubsystemMarshallingContext] {

   import SubsystemExtension._

   def readElement(reader: XMLExtendedStreamReader, list: java.util.List[ModelNode]) {
      // Require no attributes
      ParseUtils.requireNoAttributes(reader)

      //Add the main subsystem 'add' operation
      list.add(createAddSubsystemOperation)

      //Read the children
      while (reader.hasNext && reader.nextTag != END_ELEMENT) {
         if (!(reader.getLocalName == "deployment-types")) {
            throw ParseUtils.unexpectedElement(reader)
         }
         while (reader.hasNext && reader.nextTag != END_ELEMENT) {
            if (reader.isStartElement) {
               readDeploymentType(reader, list)
            }
         }
      }
   }

   private def readDeploymentType(reader: XMLExtendedStreamReader, list: java.util.List[ModelNode]): Unit = {
      if (!(reader.getLocalName == "deployment-type"))
         throw ParseUtils.unexpectedElement(reader)

      var suffix: String = null
      var tick: Long = 0
      var i: Int = 0
      while (i < reader.getAttributeCount) {
         val attr = reader.getAttributeLocalName(i)
         if (attr == "tick") {
            tick = reader.getAttributeValue(i).toLong
         } else if (attr == "suffix") {
            suffix = reader.getAttributeValue(i)
         }
         else {
            throw ParseUtils.unexpectedAttribute(reader, i)
         }
         i += 1
      }

      ParseUtils.requireNoContent(reader)
      if (suffix == null) {
         throw ParseUtils.missingRequiredElement(reader, Collections.singleton("suffix"))
      }

      val addType = new ModelNode
      addType.get(OP).set(ADD)
      val addr = PathAddress.pathAddress(
         PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
         PathElement.pathElement("type", suffix))
      addType.get(OP_ADDR).set(addr.toModelNode)
      addType.get("tick").set(tick)
      list.add(addType)
   }

   def writeContent(writer: XMLExtendedStreamWriter, ctx: SubsystemMarshallingContext) {
      // Write out the main subsystem element
      ctx.startSubsystemElement(SubsystemExtension.NAMESPACE, false)
      writer.writeStartElement("deployment-types")

      val node = ctx.getModelNode
      val nodeType = node.get("type")
      import scala.collection.JavaConversions._
      for (property <- nodeType.asPropertyList) {
         writer.writeStartElement("deployment-type")
         writer.writeAttribute("suffix", property.getName)
         val entry = property.getValue
         if (entry.hasDefined("tick")) {
            writer.writeAttribute("tick", entry.get("tick").asString)
         }
         writer.writeEndElement
      }

      //End deployment-types
      writer.writeEndElement
      //End subsystem
      writer.writeEndElement
   }

}

private object SubsystemDescribeHandler extends OperationStepHandler with DescriptionProvider {

   import SubsystemExtension._

   def execute(context: OperationContext, operation: ModelNode) {
      // Add the main operation
      context.getResult.add(createAddSubsystemOperation)

      //Add the operations to create each child
      val node = context.readModel(PathAddress.EMPTY_ADDRESS)
      import scala.collection.JavaConversions._
      for (property <- node.get("type").asPropertyList) {
         val addType: ModelNode = new ModelNode
         addType.get(OP).set(ADD)
         val addr = PathAddress.pathAddress(
            PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME),
            PathElement.pathElement("type", property.getName))
         addType.get(OP_ADDR).set(addr.toModelNode)
         if (property.getValue.hasDefined("tick")) {
            addType.get("tick").set(property.getValue.get("tick").asLong)
         }
         context.getResult.add(addType)
      }
      context.completeStep
   }

   def getModelDescription(locale: Locale) =
      CommonDescriptions.getSubsystemDescribeOperation(locale)

}