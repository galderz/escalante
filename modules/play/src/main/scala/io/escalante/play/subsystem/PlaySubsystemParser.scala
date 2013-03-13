/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.play.subsystem

import org.jboss.dmr.ModelNode
import org.jboss.as.controller.persistence.SubsystemMarshallingContext
import org.jboss.staxmapper.{XMLExtendedStreamWriter, XMLExtendedStreamReader, XMLElementWriter, XMLElementReader}
import io.escalante.logging.Log
import io.escalante.xml.JavaXmlParser

/**
 * Play subsystem parser.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class PlaySubsystemParser
    extends XMLElementReader[java.util.List[ModelNode]]
    with XMLElementWriter[SubsystemMarshallingContext]
    with Log {

  def writeContent(
      writer: XMLExtendedStreamWriter,
      ctx: SubsystemMarshallingContext) {
    debug("Write Play subsystem")

    // Write out the main subsystem element
    ctx.startSubsystemElement(PlayExtension.PLAY_SUBSYSTEM_NAMESPACE, false)

    // End subsystem
    writer.writeEndElement()
  }

  def readElement(
      reader: XMLExtendedStreamReader,
      ops: java.util.List[ModelNode]) {
    // Require no attributes
    JavaXmlParser.requireNoAttributes(reader)
    // Require no content
    JavaXmlParser.requireNoContent(reader)
    ops.add(PlayExtension.createAddSubsystemOperation)
    debug("Subsystem descriptor read, add subsystem add operation")
  }

}