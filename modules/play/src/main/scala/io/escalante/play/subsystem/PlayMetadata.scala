package io.escalante.play.subsystem

import java.io.File
import org.jboss.vfs.VirtualFile
import io.escalante.yaml.YamlParser
import io.escalante.io.FileSystem._
import java.util
import org.jboss.as.server.deployment.{DeploymentUnit, AttachmentKey, DeploymentUnitProcessingException}
import io.escalante.server.{JBossModule, Deployments}
import io.escalante.util.matching.RegularExpressions._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
trait PlayMetadata {

  def attachTo(deployment: DeploymentUnit)

  def appName: String

  def appPath: File

  def systemDependencies: Seq[JBossModule]
}

object PlayMetadata {

  private val MetadataAttachmentKey: AttachmentKey[PlayMetadata] =
    AttachmentKey.create(classOf[PlayMetadata])

  val DESCRIPTOR_SUFFIX = ".yml"

  def parse(descriptor: VirtualFile): Option[PlayMetadata] =
    parse(YamlParser.parse(descriptor),
      FileSplitRegex.split(descriptor.getName).head)

  def parse(contents: String, appName: String): Option[PlayMetadata] =
    parse(YamlParser.parse(contents), appName)

  def parse(parsed: util.Map[String, Object], appName: String): Option[PlayMetadata] = {
    if (parsed != null) {
      val playKey = "play"
      val hasPlay = parsed.containsKey(playKey)
      val tmp = parsed.get(playKey)
      if (!hasPlay) {
        None // element not present
      } else if ((hasPlay && tmp == null)) {
        throw new DeploymentUnitProcessingException(
          "Play application path required")
      } else {
        val playMeta = tmp.asInstanceOf[util.Map[String, Object]]
        val path = playMeta.get("path")
        if (path != null)
          Some(StaticPlayAppMetadata(appName, new File(path.toString)))
        else
          throw new DeploymentUnitProcessingException(
            "Play application path required")
      }
    } else {
      None
    }
  }

  def fromDeployment(deployment: DeploymentUnit): Option[PlayMetadata] = {
    val attachment = deployment.getAttachment(MetadataAttachmentKey)
    if (attachment != null) Some(attachment) else None
  }

  private case class StaticPlayAppMetadata(
      appName: String, appPath: File) extends PlayMetadata {

    def attachTo(deployment: DeploymentUnit) {
      deployment.putAttachment(MetadataAttachmentKey, this)

      // If deploying static Play app, a jar containing the applications classes
      // is expected to be found (as a result of calling Play SBT package).
      // The app is normally located in places like this:
      // [PLAY_APP_ROOT]/target/scala-2.10/play21-helloworld_2.10-1.0-SNAPSHOT.jar
      for {
        scalaDir <- findFirst(new File(appPath, "target"), ScalaFolderRegex)
        jar <- findFirst(scalaDir, JarFileRegex)
      } yield {
        Deployments.attachTo(deployment, "app-lib", jar)
      }
    }

    def systemDependencies: Seq[JBossModule] = List(
      JBossModule("play.play_2_10"),
      JBossModule("org.scala-lang.scala-library")
    )

  }

}