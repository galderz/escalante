package io.escalante.lift

import org.jboss.dmr.ModelNode
import org.jboss.as.controller.persistence.SubsystemMarshallingContext
import org.jboss.staxmapper.{XMLExtendedStreamWriter, XMLExtendedStreamReader, XMLElementWriter, XMLElementReader}
import org.jboss.as.controller.parsing.ParseUtils
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftSubsystemParser extends XMLElementReader[java.util.List[ModelNode]]
with XMLElementWriter[SubsystemMarshallingContext] with Log {

   override def writeContent(writer: XMLExtendedStreamWriter, ctx: SubsystemMarshallingContext) {
      info("Write lift subsystem")

      // Write out the main subsystem element
      ctx.startSubsystemElement(LiftExtension.NAMESPACE, false)

      //End subsystem
      writer.writeEndElement();
   }

   override def readElement(reader: XMLExtendedStreamReader, list: java.util.List[ModelNode]) {
      // Require no attributes
      ParseUtils.requireNoAttributes(reader)
      ParseUtils.requireNoContent(reader)

      info("Subsystem descriptor read, add subsystem add operation")

      // Add the main subsystem 'add' operation
      list.add(LiftExtension.createAddSubsystemOperation)
   }

}