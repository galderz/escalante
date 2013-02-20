/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante

import scala.xml.Elem

/**
 * Scala version definitions.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
sealed trait Scala {

  val groupId = "org.scala-lang"

  val artifactId = "scala-library"

  def version: String

  def minorVersion: String

  def urlVersion: String

  def artifactIdVersion: String

  def moduleXml: Elem

  def isMain: Boolean

}

object Scala {

  private val DEFAULT_SCALA = Scala2x("2.10.0", isMain = true)

  def apply(): Scala = DEFAULT_SCALA

  def apply(version: String): Scala = {
    version match {
      case v if v == DEFAULT_SCALA.version => Scala2x(version, isMain = true)
      case _ => Scala2x(version, isMain = false)
    }
  }

  def apply(parsed: java.util.Map[String, Object]): Scala = {
    if (parsed != null) {
      val tmp = parsed.get("scala")
      if (tmp != null)
        Scala(tmp.asInstanceOf[java.util.Map[String, Object]]
          .get("version").toString)
      else
        Scala()
    } else {
      Scala()
    }
  }

  private class Scala2x(
      val major: Byte,
      val minor: Byte,
      val micro: Byte,
      val isMain: Boolean) extends Scala {

    private val scalaVersion: String = s"$major.$minor.$micro"

    private val scalaUrlVersion: String = f"$major%s$minor%s$micro%s"

    private val scalaMinorVersion: String = s"$major.$minor"

    def version: String = scalaVersion

    def urlVersion: String = scalaUrlVersion

    def minorVersion: String = scalaMinorVersion

    def artifactIdVersion: String = scalaMinorVersion

    def moduleXml: Elem = {
      val slot = if (isMain) "main" else version
      <module name="org.scala-lang.scala-library" slot={slot}
              xmlns="urn:jboss:module:1.1">
        <resources>
          <resource-root path={"scala-library-" + version + ".jar"}/>
        </resources>
        <dependencies>
          <module name="javax.api"/>
          <system export="true">
            <paths>
              <path name="org/xml/sax"/>
              <path name="org/xml/sax/helpers"/>
            </paths>
          </system>
        </dependencies>
      </module>
    }

    override def toString: String = s"Scala($version)"

    override def hashCode(): Int =
      41 * (41 + major) * (41 + minor) + micro

    override def equals(obj: Any): Boolean = {
      obj match {
        case that: Scala2x =>
          that.canEqual(this) &&
              major == that.major &&
              minor == that.minor &&
              micro == that.micro
        case _ => false
      }
    }

    private def canEqual(other: Any): Boolean = other.isInstanceOf[Scala2x]

  }

  private object Scala2x {

    def apply(version: String, isMain: Boolean): Scala2x = {
      val versionNumbers = version.split('.')
      val major = versionNumbers(0).toByte
      val minor = versionNumbers(1).toByte
      val micro = versionNumbers(2).toByte
      if (minor < 10)
        new Scala2xx(major, minor, micro, isMain)
      else
        new Scala2x(major, minor, micro, isMain)
    }

  }

  private class Scala2xx(
      major: Byte,
      minor: Byte,
      micro: Byte,
      isMain: Boolean) extends Scala2x(major, minor, micro, isMain) {

    /**
     * Scala version in artifact id is the full, dotted, version.
     *
     * @return an artifact Id Scala version as a String that's friendly
     *         with Scala 2.9.x releases or earlier
     */
    override def artifactIdVersion: String = version

  }

//  private class Scala28x(
//      major: Byte,
//      minor: Byte,
//      micro: Byte) extends Scala2x(major, minor, micro, false) {
//
//    override def moduleXml: Elem = {
//      val slot = if (isMain) "main" else version
//      <module name="org.scala-lang.scala-library" slot={slot}
//              xmlns="urn:jboss:module:1.1">
//        <resources>
//          <resource-root path={"scala-library-" + version + ".jar"}/>
//        </resources>
//        <dependencies>
//          <system export="true">
//            <paths>
//              <path name="org/xml/sax"/>
//              <path name="org/xml/sax/helpers"/>
//            </paths>
//          </system>
//        </dependencies>
//      </module>
//    }
//
//  }

}