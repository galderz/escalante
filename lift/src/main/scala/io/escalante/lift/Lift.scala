/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Lift {def version: String}

case object LIFT_24 extends Lift {
  def version = "2.4"
}

case class UnknownLiftVersion(version: String) extends Lift

object LiftVersion {

  def forName(version: Option[String]): Lift = {
    version match {
      case Some("2.4") => LIFT_24
      case Some(v) => new UnknownLiftVersion(v)
      case None => LIFT_24
    }
  }

}