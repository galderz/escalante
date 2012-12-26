/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.io.util

import io.escalante.util.YamlParser
import org.junit.Test
import io.escalante.{LiftYaml, ScalaYaml}
import org.scalatest.junit.AssertionsForJUnit

/**
 * Tests parsing of Escalante descriptor.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class YamlParsertTest extends AssertionsForJUnit {

  @Test def testFullDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
        | lift:
        |   version: 2.4
      """.stripMargin

    val escalanteYaml = YamlParser.parse(descriptor)
    assert(Some(ScalaYaml("2.9.2")) === escalanteYaml.scala)
    assert(Some(LiftYaml(Some("2.4"))) === escalanteYaml.lift)
  }

  @Test def testHalfScalaDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
      """.stripMargin

    val escalanteYaml = YamlParser.parse(descriptor)
    assert(Some(ScalaYaml("2.9.2")) === escalanteYaml.scala)
    assert(None === escalanteYaml.lift)
  }

  @Test def testEmptyScalaDescriptor() {
    val descriptor =
      """
        | scala:
      """.stripMargin

    val escalanteYaml = YamlParser.parse(descriptor)
    assert(None === escalanteYaml.scala)
    assert(None === escalanteYaml.lift)
  }

  @Test def testEmptyLiftDescriptor() {
    val descriptor =
      """
        | lift:
      """.stripMargin

    val escalanteYaml = YamlParser.parse(descriptor)
    assert(None === escalanteYaml.scala)
    assert(Some(LiftYaml(None)) === escalanteYaml.lift)
  }

  @Test def testHalfLiftDescriptor() {
    val descriptor =
      """
        | lift:
        |   version: 2.4
      """.stripMargin

    val escalanteYaml = YamlParser.parse(descriptor)
    assert(None === escalanteYaml.scala)
    assert(Some(LiftYaml(Some("2.4"))) === escalanteYaml.lift)
  }

  @Test def testEmptyDescriptor() {
    val descriptor = ""
    val escalanteYaml = YamlParser.parse(descriptor)
    assert(None === escalanteYaml.scala)
    assert(None === escalanteYaml.lift)
  }

}
