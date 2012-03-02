package org.scalabox.lift

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
sealed trait Scala

case object SCALA_291 extends Scala

case object SCALA_282 extends Scala

case class UnknownScalaVersion(version: String) extends Scala

object ScalaVersion {

   def forName(version: Option[String]): Scala = {
      version match {
         case Some("2.9.1") => SCALA_291
         case Some("2.8.2") => SCALA_282
         case Some(v) => new UnknownScalaVersion(v)
         case None => SCALA_291
      }
   }

}