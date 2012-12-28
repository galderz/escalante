/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.AttachmentKey
import io.escalante._
import io.escalante.lift.{LIFT_24, Lift}
import maven.MavenArtifact

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class LiftMetaData(
  liftVersion: Lift,
  scalaVersion: Scala,
  modules: Seq[String]) {

  def mavenArtifacts: Seq[MavenArtifact] = {
    val mavenScalaVersion =
      (liftVersion, scalaVersion) match {
        case (LIFT_24, SCALA_292) => SCALA_291 // no lift artifact for 2.9.2
        case _ => scalaVersion
      }

    val customMavenArtifacts =
      for (module <- modules)
      yield liftMavenArtifact(module, mavenScalaVersion)

    liftMavenArtifact("webkit", mavenScalaVersion) +: customMavenArtifacts
  }

  private def liftMavenArtifact(module: String, scala: Scala): MavenArtifact =
    new MavenArtifact("net.liftweb",
      "lift-" + module + "_" + scala.version, liftVersion.version)

}

object LiftMetaData {

  val ATTACHMENT_KEY: AttachmentKey[LiftMetaData] =
    AttachmentKey.create(classOf[LiftMetaData])

}