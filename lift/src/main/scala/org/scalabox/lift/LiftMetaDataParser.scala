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

      // TODO: Validate it's called lift-app

      // Read until the first start element
      while (reader.hasNext() && reader.next() != START_ELEMENT) {}

      val version = LiftVersion.forName(
         readOptionalStringAttributeElement(reader, "version"))

      val scalaVersion = ScalaVersion.forName(
         readOptionalStringAttributeElement(reader, "scalaVersion"))

      new LiftMetaData(version, scalaVersion)
   }

}

//sealed trait Attribute
//case object VERSION extends Attribute
//case object SCALA_VERSION extends Attribute
//case class UnknownAttribute(attribute: String) extends Attribute
//
//object AttributeResolver {
//
//   // If this grows big, convert to a map for more efficient lookup
//
//   def forName(attribute: String): Attribute = {
//      attribute match {
//         case "version" => VERSION
//         case "scalaVersion" => SCALA_VERSION
//         case a => new UnknownAttribute(a)
//      }
//   }
//
//}