package io.escalante.lift.subsystem

import javax.xml.stream.XMLStreamReader
import javax.xml.stream.XMLStreamConstants._
import io.escalante.util.JavaXmlParser._
import io.escalante.ScalaVersion
import io.escalante.lift.LiftVersion

/**
 * Lift application metadata parser
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftMetaDataParser {

   def parse(reader: XMLStreamReader): LiftMetaData = {
      reader.require(START_DOCUMENT, null, null)

      // TODO: Validate it's called lift-app

      // Read until the first start element
      while (reader.hasNext() && reader.next() != START_ELEMENT) {}

      val version = LiftVersion.forName(
         readOptionalStringAttributeElement(reader, "version"))

      // Default Scala version based on last Lift release
      val scalaVersion = ScalaVersion.forName(
         readOptionalStringAttributeElement(reader, "scala-version")
                 .getOrElse("2.9.2"))

      new LiftMetaData(version, scalaVersion)
   }

}