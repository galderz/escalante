/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.dmr.ModelNode
import org.jboss.as.controller.persistence.SubsystemMarshallingContext
import org.jboss.as.controller.descriptions.ModelDescriptionConstants
import org.jboss.staxmapper.{XMLExtendedStreamWriter, XMLExtendedStreamReader, XMLElementWriter, XMLElementReader}
import io.escalante.logging.Log
import javax.xml.stream.XMLStreamConstants._
import annotation.tailrec
import io.escalante.util.JavaXmlParser

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftSubsystemParser
  extends XMLElementReader[java.util.List[ModelNode]]
  with XMLElementWriter[SubsystemMarshallingContext]
  with Log {

  override def writeContent(writer: XMLExtendedStreamWriter,
    ctx: SubsystemMarshallingContext) {
    debug("Write lift subsystem")

    // Write out the main subsystem element
    ctx.startSubsystemElement(LiftExtension.NAMESPACE, false)

    val model = ctx.getModelNode
    val hasPath = model.hasDefined(ThirdPartyModulesRepo.PATH)
    val hasRelativeTo = model.hasDefined(ThirdPartyModulesRepo.RELATIVE_TO)

    if (hasPath || hasRelativeTo) {
      writer.writeStartElement(ThirdPartyModulesRepo.THIRDPARTY_MODULES_REPO)
      if (hasPath) {
        writer.writeAttribute(ModelDescriptionConstants.PATH,
          model.get(ThirdPartyModulesRepo.PATH).asString())
      }

      if (hasRelativeTo) {
        writer.writeAttribute(ModelDescriptionConstants.RELATIVE_TO,
          model.get(ThirdPartyModulesRepo.RELATIVE_TO).asString())
      }
      // End thirdparty module repo
      writer.writeEndElement()
    }

    // End subsystem
    writer.writeEndElement()
  }

  override def readElement(reader: XMLExtendedStreamReader, ops: java.util.List[ModelNode]) {
    // Require no attributes
    JavaXmlParser.requireNoAttributes(reader)

    val addSubsystemOp = LiftExtension.createAddSubsystemOperation

    while (reader.hasNext && (reader.nextTag() != END_ELEMENT)) {
      val element = reader.getLocalName
      element match {
        case ThirdPartyModulesRepo.THIRDPARTY_MODULES_REPO =>
          parseThirdPartyModulesRepo(0,
            reader.getAttributeCount, reader, addSubsystemOp)
        case _ =>
          throw JavaXmlParser.unexpectedElement(reader)
      }
    }

    ops.add(addSubsystemOp)

    debug("Subsystem descriptor read, add subsystem add operation")
  }

  @tailrec
  private def parseThirdPartyModulesRepo(attrIndex: Int,
    attrCount: Int, reader: XMLExtendedStreamReader,
    addSubsystemOp: ModelNode) {
    // Check if we've gone beyond the attribute count
    if (attrIndex >= attrCount) {
      JavaXmlParser.requireNoContent(reader)
    } else {
      val value = reader.getAttributeValue(attrIndex)
      val attributeName = reader.getAttributeLocalName(attrIndex)
      val node = attributeName match {
        case ModelDescriptionConstants.RELATIVE_TO =>
          addSubsystemOp.get(ThirdPartyModulesRepo.RELATIVE_TO)
        case ModelDescriptionConstants.PATH =>
          addSubsystemOp.get(ThirdPartyModulesRepo.PATH)
      }
      node.set(value)
      parseThirdPartyModulesRepo(attrIndex + 1, attrCount,
        reader, addSubsystemOp)
    }
  }

}