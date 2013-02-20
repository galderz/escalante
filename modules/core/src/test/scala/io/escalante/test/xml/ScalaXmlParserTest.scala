/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.xml

import io.escalante.xml.ScalaXmlParser._
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test

/**
 * Scala XML parser tests.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class ScalaXmlParserTest extends AssertionsForJUnit {

  @Test def testAddToXml() {
    val xml = <a></a>
    val newXml = addXmlElement("a", <b></b>, xml)
    assert("<a><b></b></a>" === newXml.toString())
  }

  @Test def testAddUriFilteredElement() {
    val xml =
      <profile>
        <subsystem xmlns="urn:jboss:logging:1.2" />
        <subsystem xmlns="urn:jboss:configadmin:1.0"/>
      </profile>

    val newXml = addXmlElement(
      "subsystem", "urn:jboss:logging", <b></b>, xml)

    val newXmlString = newXml.toString()
    assert(newXmlString.contains(
      """<subsystem xmlns="urn:jboss:logging:1.2"><b></b></subsystem>"""),
      newXmlString)
  }

  @Test def testModifyAttribute() {
    val xml =
      <profile>
        <subsystem xmlns="urn:jboss:logging:1.2" />
          <console-handler name="CONSOLE">
            <level name="INFO"></level>
            <formatter/>
          </console-handler>
        <subsystem xmlns="urn:jboss:configadmin:1.0"/>
      </profile>

    val newXml = replaceXmlElement("console-handler",
      <console-handler name="CONSOLE">
        <level name="TRACE"></level><formatter/>
      </console-handler>, xml)

    val newXmlString = newXml.toString()
    assert(newXmlString.contains(
      """<level name="TRACE"></level><formatter/>"""), newXmlString)
  }


}
