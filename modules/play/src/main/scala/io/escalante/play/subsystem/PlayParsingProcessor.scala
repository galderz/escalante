/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.play.subsystem

import org.jboss.as.server.deployment._
import io.escalante.logging.Log
import scala.Some

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class PlayParsingProcessor extends DeploymentUnitProcessor with Log {

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit
    val descriptor = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot
    val metadata = PlayMetadata.parse(descriptor)
    metadata match {
      case None =>
        debug("Descriptor found in %s, but not a Play application", descriptor)
      case Some(playMetadata) =>
        debug("Play application detected in %s", descriptor)
        val appPath = playMetadata.appPath
        if (!appPath.exists()) {
          val path = appPath.getAbsolutePath
          throw new DeploymentUnitProcessingException(
              s"Play application path does not exist: $path")
        }

        // Attach metadata to deployment
        PlayDeployment.metadataToDeployment(playMetadata, deployment)
    }
  }

  def undeploy(context: DeploymentUnit) {
    // No-op
  }

}
