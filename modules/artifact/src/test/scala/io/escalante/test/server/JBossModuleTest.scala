/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.server

import io.escalante.Scala
import io.escalante.server.JBossModule
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import io.escalante.artifact.maven.MavenArtifact

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class JBossModuleTest extends AssertionsForJUnit {

  @Test def testScalaJBossModule() {
    val module = JBossModule(Scala())
    val moduleDep = module.moduleDependency
    assert("org.scala-lang.scala-library:main" ===
        moduleDep.getIdentifier.toString)
  }

  @Test def testScala2xMavenArtifactJBossModule() {
    val module = JBossModule(
      MavenArtifact("org.scala-lang", "scala-library", "2.9.1"))
    // Maven artifacts don't know about slots in general, so main expected...
    assert(module.moduleDirName.endsWith("org/scala-lang/scala-library/main"),
      module.moduleDirName)
  }

  @Test def testScala2xJBossModule() {
    val module = JBossModule(Scala("2.9.1"))
    assert(module.moduleDirName.endsWith("org/scala-lang/scala-library/2.9.1"),
      module.moduleDirName)
  }

}
