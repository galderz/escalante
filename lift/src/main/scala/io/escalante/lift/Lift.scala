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
sealed trait Lift {def version: String}

// TODO: Do we need a specific case for 2.4? We support 'any'...
case object LIFT_24 extends Lift {
  def version = "2.4"
}

case class UnknownLiftVersion(version: String) extends Lift

object Lift {

  private def forName(version: String): Lift = {
    version match {
      case "2.4" => LIFT_24
      case v => new UnknownLiftVersion(v)
    }
  }

  def parse(parsed: java.util.Map[String, Object]): Option[Lift] = {
    val liftKey = "lift"
    if (parsed != null) {
      val hasLift = parsed.containsKey(liftKey)
      val tmp = parsed.get(liftKey)
      if (!hasLift)
        None
      else if (hasLift && tmp == null)
        Some(LIFT_24)
      else {
        val liftMeta = tmp.asInstanceOf[util.Map[String, Object]]
        val version = liftMeta.get("version")
        if (version != null)
          Some(forName(version.toString))
        else
          Some(LIFT_24)
      }
    } else {
      None
    }
  }
}