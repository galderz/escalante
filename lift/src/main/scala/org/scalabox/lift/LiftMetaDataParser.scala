package org.scalabox.lift

import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamConstants._
import org.scalabox.util.JavaXmlParser._
import org.scalabox.ScalaVersion

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftMetaDataParser {

   def parse(reader: XMLStreamReader): LiftMetaData = {
      reader.require(START_DOCUMENT, null, null)

      // TODO: Validate it's called lift-app

      // Read until the first start element
      while (reader.hasNext() && reader.next() != START_ELEMENT) {}

      val version = LiftVersion.forName(
         readOptionalStringAttributeElement(reader, "version"))

      val scalaVersion = ScalaVersion.forName(
         readOptionalStringAttributeElement(reader, "scala-version"))

      new LiftMetaData(version, scalaVersion)
   }

}