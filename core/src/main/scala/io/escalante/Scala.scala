package io.escalante

import maven.MavenArtifact
import xml.Elem

/**
 * Scala version definitions.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
sealed trait Scala {
   def version: String
   def moduleXml: Elem
   def isMain: Boolean = false
   val maven = new MavenArtifact(
      "org.scala-lang", "scala-library", version, isMain)
}

case object SCALA_291 extends Scala {
   override def version = "2.9.1"
   override def isMain = true
   override def moduleXml =
      <module name="org.scala-lang.scala-library"
              xmlns="urn:jboss:module:1.0">
         <resources>
            <resource-root path="scala-library-2.9.1.jar"/>
         </resources>
         <dependencies>
            <module name="javax.api"/>
         </dependencies>
      </module>
}

case object SCALA_282 extends Scala {
   override def version = "2.8.2"
   override def moduleXml =
      <module name="org.scala-lang.scala-library" slot="2.8.2"
              xmlns="urn:jboss:module:1.0">
         <resources>
            <resource-root path="scala-library-2.8.2.jar"/>
         </resources>
         <dependencies>
            <system export="true">
               <paths>
                  <path name="org/xml/sax"/>
                  <path name="org/xml/sax/helpers"/>
               </paths>
            </system>
         </dependencies>
      </module>
}

case class UnknownScalaVersion(version: String) extends Scala{
   override def moduleXml = null
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