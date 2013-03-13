/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.server

import java.io.File
import org.jboss.as.server.deployment.{Attachments, DeploymentUnit}
import org.jboss.as.server.deployment.module.{MountHandle, TempFileProviderService, ResourceRoot, ModuleRootMarker}
import io.escalante.logging.Log
import org.jboss.vfs.{VFS, VirtualFile}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object Deployments extends Log {

  def attachTo(deployment: DeploymentUnit, mount: String, files: File*) {
    val resourceRoot = deployment
        .getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot

    files.foreach {
      file =>
        trace(s"Attaching $file to $mount")
        val temp = resourceRoot.getChild(mount) // Virtual mount point
        val repackagedJar = createZipRoot(temp, file)
        ModuleRootMarker.mark(repackagedJar)
        deployment.addToAttachmentList(
          Attachments.RESOURCE_ROOTS, repackagedJar)
    }
  }

  /**
   * Creates a Zip root under the virtual mount point to store the file.
   */
  private def createZipRoot(
      deploymentTemp: VirtualFile,
      file: File): ResourceRoot = {
    val archive = deploymentTemp.getChild(file.getName)
    val closable = VFS.mountZip(
      file, archive, TempFileProviderService.provider())
    new ResourceRoot(file.getName, archive, new MountHandle(closable))
  }

}
