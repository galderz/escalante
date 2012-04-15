package org.scalabox

import assembly.{MavenArtifact, JBossModule}


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

case object SCALA_29 extends Scala {
   def version: String = "2.9.1"
   def maven: MavenArtifact = maven(true)
}

case object SCALA_28 extends Scala {
   def version: String = "2.8.2"
   def maven: MavenArtifact = maven(false)
}

case class UnknownScalaVersion(version: String) extends Scala{
   def maven: MavenArtifact = null
}

object ScalaVersion {

   def forName(version: Option[String]): Scala = {
      version match {
         case Some("2.9.1") => SCALA_29
         case Some("2.8.2") => SCALA_28
         case Some(v) => new UnknownScalaVersion(v)
         case None => SCALA_29
      }
   }

}