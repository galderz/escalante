/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Test
import io.escalante.play.subsystem.PlayMetadata
import java.io.File
import org.jboss.as.server.deployment.DeploymentUnitProcessingException
import io.escalante.Scala

/**
 * Tests parsing of Escalante descriptor.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class PlayMetadataParserTest extends AssertionsForJUnit {

  @Test def testFullDescriptor() {
    val descriptor =
      """
        | scala:
        |   version: 2.9.2
        | play:
        |   path: /path/to/application
        |   modules:
        |     - play-jdbc
        |     - anorm
      """.stripMargin
    val meta = PlayMetadata.parse(descriptor, "my-app").get
    assert(Scala("2.9.2") === meta.scalaVersion)
    assert("my-app" === meta.appName)
    assert(new File("/path/to/application") === meta.appPath)
    assert(List("play-jdbc", "anorm") === meta.modules)
  }

  @Test def testNoModules() {
    val descriptor =
      """
        | play:
        |   path: /path/to/application
      """.stripMargin
    val meta = PlayMetadata.parse(descriptor, "your-app").get
    assert(Scala("2.10.0") === meta.scalaVersion)
    assert("your-app" === meta.appName)
    assert(new File("/path/to/application") === meta.appPath)
    assert(List() === meta.modules)
  }

  @Test(expected = classOf[DeploymentUnitProcessingException])
  def testNoPathDescriptor() {
    val descriptor =
      """
        | play:
      """.stripMargin
    PlayMetadata.parse(descriptor, "boo")
  }

  @Test def testNoPlayDescriptor() {
    val descriptor =
      """
      """.stripMargin
    val meta = PlayMetadata.parse(descriptor, "any")
    assert(None === meta)
  }

}