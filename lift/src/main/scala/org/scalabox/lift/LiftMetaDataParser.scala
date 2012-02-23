package org.scalabox.lift

import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamConstants._
import org.scalabox.util.XmlParser._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftMetaDataParser {

   def parse(reader: XMLStreamReader): LiftMetaData = {
      reader.require(START_DOCUMENT, null, null)

      // Read until the first start element
      while (reader.hasNext() && reader.next() != START_ELEMENT) {}

      val version = LiftVersionResolver.toVersion(
         readStringAttributeElement(reader, "version"))

      // TODO: Parse scala version too...
      new LiftMetaData(version, SCALA_291)
   }

}