package org.scalabox.lift

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait ScalaVersion

case object SCALA_291 extends ScalaVersion

case class UnknownScalaVersion(version: String) extends ScalaVersion

object ScalaResolver {

   private val versions = Map[String, ScalaVersion] {
      "2.9.1" -> SCALA_291
   }

   def toVersion(version: String): ScalaVersion =
      versions.getOrElse(version, new UnknownScalaVersion(version))

}