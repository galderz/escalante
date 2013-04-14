/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.lift

import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import io.escalante.Scala
import io.escalante.lift.Lift
import io.escalante.lift.subsystem.LiftMetadata

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
        |   version: 2.3
        |   modules:
        |     - record
        |     - mongodb
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(Lift("2.3") === meta.get.liftVersion)
    assert(Scala("2.9.2") === meta.get.scalaVersion)
    assert(List("record", "mongodb") === meta.get.modules)

    assert(LiftMetadata.parse(descriptor, isImplicitJpa = true).get.modules
      === List("jpa", "record", "mongodb"))
  }

  @Test def testHalfScalaDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(None === meta)
  }

  @Test def testEmptyScalaDescriptor() {
    val descriptor =
      """
        | scala:
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(None === meta)
  }

  @Test def testEmptyLiftDescriptor() {
    val descriptor =
      """
        | lift:
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(Lift() === meta.get.liftVersion)
    assert(Scala() === meta.get.scalaVersion)
    assert(List() === meta.get.modules)
  }

  @Test def testLiftOnlyVersionDescriptor() {
    val descriptor =
      """
        | lift:
        |   version: 2.2
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(Lift("2.2") === meta.get.liftVersion)
    assert(Scala() === meta.get.scalaVersion)
    assert(List() === meta.get.modules)
  }

  @Test def testLiftOnlyModulesDescriptor() {
    val descriptor =
      """
        | lift:
        |   modules:
        |     - mapper
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(Lift() === meta.get.liftVersion)
    assert(Scala() === meta.get.scalaVersion)
    assert(List("mapper") === meta.get.modules)
  }

  @Test def testLiftJpaModuleDescriptor() {
    val descriptor =
      """
        | lift:
        |   modules:
        |     - jpa
      """.stripMargin

    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(List("jpa") === meta.get.modules)
  }

  @Test def testEmptyDescriptor() {
    val descriptor = ""
    val meta = LiftMetadata.parse(descriptor, isImplicitJpa = false)
    assert(None === meta)
  }

}
