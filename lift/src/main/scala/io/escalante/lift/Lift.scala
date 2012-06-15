package io.escalante.lift

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Lift { def version: String }

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