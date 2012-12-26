/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante

/**
 * Escalante descriptor base classes
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
case class EscalanteYaml(
  scala: Option[ScalaYaml],
  lift: Option[LiftYaml]
)

case class ScalaYaml(
  version: String
)

case class LiftYaml(
  version: Option[String]
)

object EscalanteYaml {
  val ESCALANTE_YAML = "META-INF/escalante.yml"
}