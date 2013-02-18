/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.xml

import java.io.File
import scala.xml.transform.{RuleTransformer, RewriteRule}
import scala.xml._
import scala.Predef._

/**
 * Scala XML parser util class.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object ScalaXmlParser {

  def addXmlElement(parentElem: String, element: Node, xmlFile: File): Node =
    addXmlElement(parentElem, element, XML.loadFile(xmlFile))

  def addXmlElement(parentElem: String, element: Node, xml: Node): Node =
    addXmlRules(xml, new AddChildrenTo(parentElem, null, element))

  def addXmlElement(
      parentElem: String,
      parentUri: String,
      element: Node,
      xml: Node): Node =
    addXmlRules(xml, new AddChildrenTo(parentElem, parentUri, element))

  def addXmlElements(parentElem: String, elements: Seq[Node], xml: Node): Node =
    addXmlRules(xml, elements.map(new AddChildrenTo(parentElem, null, _)): _*)

  def replaceXmlElement(elementToReplace: String, element: Node, xml: Node) =
    addXmlRules(xml, new ReplaceElement(elementToReplace, element))

  def saveXml(fileName: String, xmlNode: Node): Any =
    XML.save(fileName, xmlNode, "UTF-8", xmlDecl = true, doctype = null)

  def saveXml(file: File, xmlNode: Node): Any =
    saveXml(file.getCanonicalPath, xmlNode)

  private def addXmlRules(xml: Node, rules: RewriteRule*): Node =
    new RuleTransformer(rules: _*).transform(xml).head

  private def addChild(n: Node, newChild: Node) = n match {
    case Elem(prefix, label, attribs, scope, child@_*) =>
      Elem(prefix, label, attribs, scope, child ++ newChild: _*)
    case _ => sys.error("Can only add children to elements!")
  }

  private def replaceElement(n: Node, newElement: Node) = n match {
    case Elem(prefix, label, attribs, scope, child@_*) => newElement
    case _ => sys.error("Can only add children to elements!")
  }
  private class AddChildrenTo(
      label: String,
      uri: String,
      newChild: Node) extends RewriteRule {
    override def transform(n: Node) = n match {
      case n@Elem(_, `label`, _, _, _*)
            if uri == null =>
        addChild(n, newChild)
      case n@Elem(_, `label`, _, _, _*)
            if uri != null && n.scope.uri.startsWith(uri) =>
        addChild(n, newChild)
      case other => other
    }
  }

  private class ReplaceElement(
      label: String,
      newElement: Node) extends RewriteRule {
    override def transform(n: Node) = n match {
      case n@Elem(_, `label`, _, _, _*) => replaceElement(n, newElement)
      case other => other
    }
  }

}
