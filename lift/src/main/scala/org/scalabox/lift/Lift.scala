package org.scalabox.lift

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Lift

case object LIFT_24 extends Lift

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