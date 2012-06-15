package io.escalante.util

import java.io.File
import scala.xml.transform.{RuleTransformer, RewriteRule}
import scala.xml._
import annotation.tailrec
import scala.Predef._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ScalaXmlParser {

   def addXmlElement(parentElem: String, element: Node, xmlFile: File): Node =
      addXmlElement(parentElem, element, XML.loadFile(xmlFile))

   def addXmlElement(parentElem: String, element: Node, xml: Node): Node =
      addXmlRules(xml, new AddChildrenTo(parentElem, element))

   def addXmlElements(parentElem: String, elements: Seq[Node], xml: Node): Node =
      addXmlRules(xml, elements.map(new AddChildrenTo(parentElem, _)): _*)

   def addXmlAttribute(elem: Elem, name: String, value: String): Elem =
      elem % Attribute(None, name, Text(value), Null)

   @tailrec
   def addXmlAttributes(elem: Elem, attributes: (String, String)*): Elem = {
      if (attributes == Nil) elem
      else {
         val attr = attributes.head
         addXmlAttributes(addXmlAttribute(elem, attr._1, attr._2),
            attributes.tail : _*)
      }
   }

   def saveXml(fileName: String, xmlNode: Node): Any =
      XML.save(fileName, xmlNode, "UTF-8", true, null)

   def saveXml(file: File, xmlNode: Node): Any =
      saveXml(file.getCanonicalPath, xmlNode)

   private def addXmlRules(xml: Node, rules: RewriteRule*): Node =
      new RuleTransformer(rules: _*).transform(xml).head

   private def addChild(n: Node, newChild: Node) = n match {
      case Elem(prefix, label, attribs, scope, child @ _*) =>
         Elem(prefix, label, attribs, scope, child ++ newChild : _*)
      case _ => error("Can only add children to elements!")
   }

   private class AddChildrenTo(label: String, newChild: Node) extends RewriteRule {

      override def transform(n: Node) = n match {
         case n @ Elem(_, `label`, _, _, _*) => addChild(n, newChild)
         case other => other
      }

   }

}
