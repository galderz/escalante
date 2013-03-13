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
import org.jboss.vfs.VirtualFile
import io.escalante.yaml.YamlParser
import collection.JavaConversions._
import scala.Some
import io.escalante.server.JBossModule
import io.escalante.util.matching.RegularExpressions

/**
 * Lift metadata for a particular deployment.
 *
 * @author Galder Zamarreño
 * @since 1.0
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

object LiftMetaData {

  val ATTACHMENT_KEY: AttachmentKey[LiftMetaData] =
    AttachmentKey.create(classOf[LiftMetaData])

  def parse(descriptor: VirtualFile, isImplicitJpa: Boolean): Option[LiftMetaData] =
    parse(YamlParser.parse(descriptor), isImplicitJpa)

  def parse(contents: String, isImplicitJpa: Boolean): Option[LiftMetaData] =
    parse(YamlParser.parse(contents), isImplicitJpa)

  private def parse(
      parsed: util.Map[String, Object],
      isImplicitJpa: Boolean): Option[LiftMetaData] = {
    val scala = Scala(parsed)
    val lift = Lift(parsed)

    lift match {
      case Some(l) =>
        // If lift key found, check if modules present
        val liftMeta = parsed.get("lift").asInstanceOf[util.Map[String, Object]]
        val modules = extractModules(liftMeta, isImplicitJpa)
        Some(LiftMetaData(l, scala, modules))
      case None =>
        None
    }
  }

  private def extractModules(
      liftMeta: util.Map[String, Object],
      isImplicitJpa: Boolean): Seq[String] = {
    val modules = new util.ArrayList[String]()
    // Add jpa module if implicitly enabled
    if (isImplicitJpa)
      modules.add("jpa")

    if (liftMeta != null) {
      val modulesMeta = liftMeta.get("modules")
      if (modulesMeta != null) {
        modules.addAll(modulesMeta.asInstanceOf[util.List[String]])
      }
    }

    asScalaBuffer(modules).toSeq
  }

}