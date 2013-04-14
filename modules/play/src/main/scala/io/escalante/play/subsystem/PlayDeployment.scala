package io.escalante.play.subsystem

import org.jboss.as.server.deployment.{AttachmentKey, DeploymentUnit}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object PlayDeployment {

  private val MetadataAttachmentKey: AttachmentKey[PlayMetadata] =
    AttachmentKey.create(classOf[PlayMetadata])

  def metadataToDeployment(metadata: PlayMetadata, deployment: DeploymentUnit) {
    deployment.putAttachment(MetadataAttachmentKey, metadata)
  }

  def metadataFromDeployment(deployment: DeploymentUnit): Option[PlayMetadata] = {
    val attachment = deployment.getAttachment(MetadataAttachmentKey)
    if (attachment != null) Some(attachment) else None
  }

}