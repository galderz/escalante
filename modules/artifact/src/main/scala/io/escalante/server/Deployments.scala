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
import io.escalante.artifact.maven.{MavenDependencyResolver, MavenArtifact}

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object Deployments extends Log {

  /**
   *
   * @param deployment
   * @param mountPoint
   * @param artifacts
   */
  def attachArtifacts(
      deployment: DeploymentUnit,
      mountPoint: String,
      artifacts: Seq[MavenArtifact]) {
    // TODO: Parallelize with Scala 2.10 futures...
    val jars =
      for {
        artifact <- artifacts
        // TODO: add more clever logic to resolveArtifact:
        //  - if lift 2.4 + scala 2.9.2 does not exist, check is scala version is latest
        //  - if it is, try "decreasing version", so "2.9.1"... that way all the way down
        //  - if it's not latest, try latest and then others
        resolvedJars <- MavenDependencyResolver.resolveArtifact(artifact)
      } yield {
        resolvedJars
      }

    // Remove duplicates to avoid duplicate mount errors
    attachJars(deployment, mountPoint, jars.distinct)
  }

  /**
   *
   * @param deployment
   * @param mount
   * @param jars
   */
  def attachJars(deployment: DeploymentUnit, mount: String, jars: Seq[File]) {
    val resourceRoot = deployment
        .getAttachment(Attachments.DEPLOYMENT_ROOT).getRoot

    for (
      jar <- jars
    ) yield {
      trace(s"Attaching $jar to $mount")
      val temp = resourceRoot.getChild(mount) // Virtual mount point
      val repackagedJar = createZipRoot(temp, jar)
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
