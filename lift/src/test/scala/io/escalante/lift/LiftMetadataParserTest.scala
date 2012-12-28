/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import subsystem.LiftMetaDataParser
import io.escalante.SCALA_292

/**
 * Tests parsing of Escalante descriptor.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftMetadataParserTest extends AssertionsForJUnit {

  @Test def testFullDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
        | lift:
        |   version: 2.4
        |   modules:
        |     - record
        |     - mongodb
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta.get.liftVersion === LIFT_24)
    assert(meta.get.scalaVersion === SCALA_292)
    assert(meta.get.modules === List("record", "mongodb"))
  }

  @Test def testHalfScalaDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta === None)
  }

  @Test def testEmptyScalaDescriptor() {
    val descriptor =
      """
        | scala:
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta === None)
  }

  @Test def testEmptyLiftDescriptor() {
    val descriptor =
      """
        | lift:
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta.get.liftVersion === LIFT_24)
    assert(meta.get.scalaVersion === SCALA_292)
    assert(meta.get.modules === List())
  }

  @Test def testLiftOnlyVersionDescriptor() {
    val descriptor =
      """
        | lift:
        |   version: 2.4
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta.get.liftVersion === LIFT_24)
    assert(meta.get.scalaVersion === SCALA_292)
    assert(meta.get.modules === List())
  }

  @Test def testLiftOnlyModulesDescriptor() {
    val descriptor =
      """
        | lift:
        |   modules:
        |     - mapper
      """.stripMargin

    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta.get.liftVersion === LIFT_24)
    assert(meta.get.scalaVersion === SCALA_292)
    assert(meta.get.modules === List("mapper"))
  }

  @Test def testEmptyDescriptor() {
    val descriptor = ""
    val meta = LiftMetaDataParser.parse(descriptor)
    assert(meta === None)
  }

}
