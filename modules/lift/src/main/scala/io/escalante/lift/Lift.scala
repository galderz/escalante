/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import io.escalante.yaml.YamlParser

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Lift {

  def version: String

}

object Lift {

  private val DEFAULT = Lift2x("2.5")

  def apply(): Lift = DEFAULT

  def apply(version: String): Lift = Lift2x(version)

  def apply(parsed: java.util.Map[String, Object]): Option[Lift] = {
    for (
      version <- YamlParser.detectFramework("lift", DEFAULT.version, parsed)
    ) yield {
      Lift(version)
    }
  }

  private case class Lift2x(version: String) extends Lift

}