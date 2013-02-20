/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.AttachmentKey
import io.escalante.lift.Lift
import io.escalante.Scala
import io.escalante.artifact.maven.{RegexDependencyFilter, MavenArtifact}
import org.sonatype.aether.graph.DependencyNode
import java.util
import scala.util.matching.Regex
import io.escalante.artifact.JBossModule

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
    // Generate maven artifacts based on the Scala version given by the user
    // If lift+scala combo does not exist, the artifact repository will be
    // clever enough to figure out the right combo.

    val customMavenArtifacts =
      for (module <- modules)
      yield liftMavenArtifact(module, scalaVersion)

    liftMavenArtifact("webkit", scalaVersion) +: customMavenArtifacts
  }

  def systemDependencies: Map[JBossModule, MavenArtifact] = {
    Map(
      JBossModule("org.slf4j")
          -> MavenArtifact("org.slf4j", "slf4j-api"),
      JBossModule("org.joda.time")
          -> MavenArtifact("joda-time", "joda-time"),
      JBossModule("javax.mail.api")
          -> MavenArtifact("javax.mail", "mail"),
      JBossModule("javax.activation.api")
          -> MavenArtifact("javax.activation", "activation")
    )
  }

  private def liftMavenArtifact(module: String, scala: Scala): MavenArtifact = {
    val artifactIdVersion = scala.artifactIdVersion
    val artifactId = f"lift-$module%s_$artifactIdVersion%s"
    new MavenArtifact("net.liftweb", artifactId, liftVersion.version,
      Some(new LiftDependencyFilter(systemDependencies)))
  }

  private class LiftDependencyFilter(
      systemModules: Map[JBossModule, MavenArtifact]) extends RegexDependencyFilter {

    import LiftDependencyFilter.REGEX

    def createRegex: Regex = REGEX

    override def accept(
        node: DependencyNode,
        parents: util.List[DependencyNode]): Boolean = {
      // First phase, check regular expression
      val firstPhaseAccept = super.accept(node, parents)
      if (firstPhaseAccept) {
        // If first phase accepted it, check whether it's a system dependency
        val secondPhaseAccept = !systemModules.exists( entry =>
            entry._2.groupId == node.getDependency.getArtifact.getGroupId &&
            entry._2.artifactId == node.getDependency.getArtifact.getArtifactId)
        return secondPhaseAccept
      }

      false
    }

  }

  private object LiftDependencyFilter {
    val REGEX = new Regex(
      "^(?!.*(scala-compiler|scala-library|scala-reflect|scalap|specs2*)).*$")
  }

}

object LiftMetaData {

  val ATTACHMENT_KEY: AttachmentKey[LiftMetaData] =
    AttachmentKey.create(classOf[LiftMetaData])

}