package org.scalabox.util

import java.io.File
import scala.xml.transform.{RuleTransformer, RewriteRule}
import scala.xml.{XML, Node, Elem}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ScalaXmlParser {

   def addXmlElement(parentElem: String, element: Node, xmlFile: File): Node =
      addXmlElement(parentElem, element, XML.loadFile(xmlFile))

   def addXmlElement(parentElem: String, element: Node, xmlNode: Node): Node = {
      new RuleTransformer(new AddChildrenTo(parentElem, element))
            .transform(xmlNode).head
   }
   
   def saveXml(fileName: String, xmlNode: Node) =
      XML.save(fileName, xmlNode, "UTF-8", true, null)

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
