package org.scalabox.lift.extension

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait LiftVersion

case object LIFT_24 extends LiftVersion

case class UnknownLiftVersion(version: String) extends LiftVersion

object LiftVersionResolver {

   private val versions = Map[String, LiftVersion] {
      "2.4" -> LIFT_24
   }

   // TODO: Cope with default LIFT version...

   def toVersion(version: String): LiftVersion =
      versions.getOrElse(version, new UnknownLiftVersion(version))

}