package org.scalabox

import maven.MavenArtifact

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */

sealed trait Scala {
   def version: String
   def maven: MavenArtifact
   protected def maven(isMain: Boolean): MavenArtifact =
      new MavenArtifact("org.scala-lang", "scala-library", version, isMain)
}

case object SCALA_291 extends Scala {
   def version = "2.9.1"
   def maven = maven(true)
}

case object SCALA_282 extends Scala {
   def version = "2.8.2"
   def maven = maven(false)
}

case class UnknownScalaVersion(version: String) extends Scala{
   def maven: MavenArtifact = null
}

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