/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.artifact

import org.junit.Test
import io.escalante.Scala
import io.escalante.artifact.JBossModule
import org.scalatest.junit.AssertionsForJUnit

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class JBossModuleTest extends AssertionsForJUnit {

  @Test def testScalaJBossModule() {
    val scalaJBossModule = JBossModule(Scala())
    val moduleDep = scalaJBossModule.moduleDependency
    assert("org.scala-lang.scala-library:main" ===
        moduleDep.getIdentifier.toString)
  }

}
