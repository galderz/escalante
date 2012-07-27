/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante

import maven.MavenArtifact
import scala.xml.Elem

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

abstract class SCALA_29x extends Scala {
  override def moduleXml: Elem = {
    val slot = if (isMain) "main" else version
    <module name="org.scala-lang.scala-library" slot={slot}
            xmlns="urn:jboss:module:1.1">
      <resources>
        <resource-root path={"scala-library-" + version + ".jar"}/>
      </resources>
      <dependencies>
        <module name="javax.api"/>
      </dependencies>
    </module>
  }
}

case object SCALA_290 extends SCALA_29x {
  override def version = "2.9.0"
}

case object SCALA_291 extends SCALA_29x {
  override def version = "2.9.1"
}

case object SCALA_292 extends SCALA_29x {
  override def version = "2.9.2"

  override def isMain = true
}

abstract class SCALA_28x extends Scala {
  override def moduleXml: Elem = {
    val slot = if (isMain) "main" else version
    <module name="org.scala-lang.scala-library" slot={slot}
            xmlns="urn:jboss:module:1.1">
      <resources>
        <resource-root path={"scala-library-" + version + ".jar"}/>
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
}

case object SCALA_280 extends SCALA_28x {
  override def version = "2.8.0"
}

case object SCALA_281 extends SCALA_28x {
  override def version = "2.8.1"
}

case object SCALA_282 extends SCALA_28x {
  override def version = "2.8.2"
}

case class UnknownScalaVersion(version: String) extends Scala {
  override def moduleXml = null
}

object ScalaVersion {

  def forName(version: String): Scala = {
    version match {
      case "2.9.2" => SCALA_292
      case "2.8.2" => SCALA_282
      case "2.9.1" => SCALA_291
      case "2.8.1" => SCALA_281
      case "2.9.0" => SCALA_290
      case "2.8.0" => SCALA_280
      case v => new UnknownScalaVersion(v)
    }
  }

}