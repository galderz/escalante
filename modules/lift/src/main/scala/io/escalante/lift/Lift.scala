/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import java.util

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Lift {

  def version: String

}

object Lift {

  private val DEFAULT_LIFT = Lift2x("2.4")

  def apply(): Lift = DEFAULT_LIFT

  def apply(version: String): Lift = Lift2x(version)

  def apply(parsed: java.util.Map[String, Object]): Option[Lift] = {
    val liftKey = "lift"
    if (parsed != null) {
      val hasLift = parsed.containsKey(liftKey)
      val tmp = parsed.get(liftKey)
      if (!hasLift)
        None
      else if (hasLift && tmp == null)
        Some(Lift())
      else {
        val liftMeta = tmp.asInstanceOf[util.Map[String, Object]]
        val version = liftMeta.get("version")
        if (version != null)
          Some(Lift(version.toString))
        else
          Some(Lift())
      }
    } else {
      None
    }
  }

  private case class Lift2x(version: String) extends Lift

}