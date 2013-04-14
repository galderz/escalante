/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.{DeploymentUnit, AttachmentKey}
import io.escalante.lift.Lift
import io.escalante.Scala
import io.escalante.artifact.maven.{RegexDependencyFilter, MavenArtifact}
import org.sonatype.aether.graph.DependencyNode
import java.util
import scala.util.matching.Regex
import org.jboss.vfs.VirtualFile
import io.escalante.yaml.YamlParser
import scala.Some
import io.escalante.server.JBossModule
import io.escalante.util.matching.RegularExpressions

/**
 * Lift metadata for a particular deployment.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
case class LiftMetadata(
  liftVersion: Lift,
  scalaVersion: Scala,
  modules: Seq[String]) {

  import LiftMetadata._

  def libraryDependencies: Seq[MavenArtifact] = {
    // Generate maven artifacts based on the Scala version given by the user
    // If lift+scala combo does not exist, the artifact repository will be
    // clever enough to figure out the right combo.

    val customMavenArtifacts =
      for (module <- modules)
      yield liftMavenArtifact(module, scalaVersion)

    liftMavenArtifact("webkit", scalaVersion) +: customMavenArtifacts
  }

  def systemDependencies: Map[JBossModule, MavenArtifact] = Map(
    JBossModule("org.slf4j")
        -> MavenArtifact("org.slf4j", "slf4j-api"),
    JBossModule("org.joda.time")
        -> MavenArtifact("joda-time", "joda-time"),
    JBossModule("javax.mail.api")
        -> MavenArtifact("javax.mail", "mail"),
    JBossModule("javax.activation.api")
        -> MavenArtifact("javax.activation", "activation")
  )

  def addToDeployment(deployment: DeploymentUnit) {
    deployment.putAttachment(MetadataAttachmentKey, this)
  }

  private def liftMavenArtifact(module: String, scala: Scala): MavenArtifact = {
    val artifactIdVersion = scala.artifactIdVersion
    val artifactId = f"lift-$module%s_$artifactIdVersion%s"
    new MavenArtifact("net.liftweb", artifactId, liftVersion.version,
      Some(new LiftDependencyFilter(systemDependencies)))
  }

  private class LiftDependencyFilter(
      systemModules: Map[JBossModule, MavenArtifact])
      extends RegexDependencyFilter {

    def createRegex: Regex = RegularExpressions.NotProvidedByServerRegex

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

}

object LiftMetadata {

  private val MetadataAttachmentKey: AttachmentKey[LiftMetadata] =
    AttachmentKey.create(classOf[LiftMetadata])

  def parse(descriptor: VirtualFile, isImplicitJpa: Boolean): Option[LiftMetadata] =
    parse(YamlParser.parse(descriptor), isImplicitJpa)

  def parse(contents: String, isImplicitJpa: Boolean): Option[LiftMetadata] =
    parse(YamlParser.parse(contents), isImplicitJpa)

  private def parse(
      parsed: util.Map[String, Object],
      isImplicitJpa: Boolean): Option[LiftMetadata] = {
    for (
      lift <- Lift(parsed)
    ) yield {
      // If lift key found, check if modules present
      val liftMeta = parsed.get("lift").asInstanceOf[util.Map[String, Object]]
      val modules = YamlParser.extractModules(liftMeta)
      LiftMetadata(lift, Scala(parsed),
          if (isImplicitJpa) "jpa" +: modules else modules)
    }
  }

  def fromDeployment(deployment: DeploymentUnit): Option[LiftMetadata] = {
    val attachment = deployment.getAttachment(MetadataAttachmentKey)
    if (attachment != null) Some(attachment) else None
  }

}