/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.play

import java.util
import io.escalante.yaml.YamlParser

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Play {

  def version: String

}

object Play {

  private val DEFAULT = Play21x("2.1.0")

  def apply(): Play = DEFAULT

  def apply(version: String): Play = Play21x(version)

  def apply(parsed: java.util.Map[String, Object]): Option[Play] = {
    for (
      version <- YamlParser.detectFramework("play", DEFAULT.version, parsed)
    ) yield {
      Play(version)
    }
  }

  private case class Play21x(version: String) extends Play

}
