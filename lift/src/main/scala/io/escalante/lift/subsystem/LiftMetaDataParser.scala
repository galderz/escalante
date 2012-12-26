/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import io.escalante.{ScalaYaml, EscalanteYaml, ScalaVersion}
import io.escalante.lift.LiftVersion

/**
 * Lift application metadata parser
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftMetaDataParser {

  def parse(yaml: EscalanteYaml): LiftMetaData = {
    // If reached this far, it's a Lift app
    val version = LiftVersion.forName(yaml.lift.get.version)

    // Default Scala version based on last Lift release
    val scalaVersion = ScalaVersion.forName(
      yaml.scala.getOrElse(ScalaYaml("2.9.2")).version)

    new LiftMetaData(version, scalaVersion)
  }

}