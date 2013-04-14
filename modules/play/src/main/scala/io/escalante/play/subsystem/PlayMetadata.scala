package io.escalante.play.subsystem

import io.escalante.Scala
import io.escalante.artifact.maven.{RegexDependencyFilter, MavenArtifact}
import io.escalante.io.FileSystem._
import io.escalante.play.Play
import io.escalante.server.JBossModule
import io.escalante.util.matching.RegularExpressions
import io.escalante.util.matching.RegularExpressions._
import io.escalante.yaml.YamlParser
import java.io.File
import java.util
import scala.Some
import scala.util.matching.Regex
import org.jboss.vfs.VirtualFile
import org.jboss.as.server.deployment.DeploymentUnitProcessingException
import org.sonatype.aether.graph.DependencyNode

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class PlayMetadata(
    playVersion: Play,
    scalaVersion: Scala,
    appName: String,
    appPath: File,
    modules: Seq[String]) {

  def applicationJar: Option[File] = {
    for {
      scalaDir <- findFirst(new File(appPath, "target"), ScalaFolderRegex)
      jar <- findFirst(scalaDir, JarFileRegex)
    } yield jar
  }

  def libraryDependencies: Seq[MavenArtifact] = {
    // Generate maven artifacts based on the Scala version given by the user
    for (
      module <- modules
    ) yield {
      playMavenArtifact(module, scalaVersion)
    }
  }

  def systemDependencies: Map[JBossModule, MavenArtifact] = Map(
    JBossModule("play.play_2_10")
        -> MavenArtifact("play", "play_2.10"),
    JBossModule("org.scala-lang.scala-library")
        -> MavenArtifact("org.scala-lang", "scala-library")
  )

  private def playMavenArtifact(module: String, scala: Scala): MavenArtifact = {
    val artifactIdVersion = scala.artifactIdVersion
    val artifactId = f"$module%s_$artifactIdVersion%s"
    // TODO: Add exclusions for h2, hibernate-validator, javax.sevlet.api... check play-jbdc pom
    // And provide those as system dependencies, just like for Lift metadata
    new MavenArtifact("play", artifactId, playVersion.version,
      Some(new PlayDependencyFilter(systemDependencies)))
  }

  // TODO: Dup with LiftDependencyFilter
  private class PlayDependencyFilter(
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

object PlayMetadata {

  val DESCRIPTOR_SUFFIX = ".yml"

  def parse(descriptor: VirtualFile): Option[PlayMetadata] =
    parse(YamlParser.parse(descriptor),
      FileSplitRegex.split(descriptor.getName).head)

  def parse(contents: String, appName: String): Option[PlayMetadata] =
    parse(YamlParser.parse(contents), appName)

  def parse(parsed: util.Map[String, Object], appName: String): Option[PlayMetadata] = {
    for (
      play <- Play(parsed)
    ) yield {
      val playMeta = parsed.get("play").asInstanceOf[util.Map[String, Object]]
      if (playMeta != null && playMeta.containsKey("path")) {
        val path = playMeta.get("path")
        PlayMetadata(play, Scala(parsed), appName,
          new File(path.toString), YamlParser.extractModules(playMeta))
      } else {
        throw new DeploymentUnitProcessingException(
          "Play application path required")
      }
    }
  }

}