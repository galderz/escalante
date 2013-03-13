/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.play.subsystem

import org.jboss.as.server.deployment._
import module.{MountHandle, ModuleSpecification, ResourceRoot}
import org.jboss.as.server.deployment.Attachments._
import org.jboss.vfs.{VFSUtils, VFS}
import java.io.{Closeable, IOException}
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 * @see This class is based on Polyglot's
 *      [[org.projectodd.polyglot.core.processors.DescriptorRootMountProcessor]],
 *      consider bringing in the project if more classes used...
 */
class PlayRootMountProcessor extends DeploymentUnitProcessor with Log {

  import PlayRootMountProcessor._

  def deploy(ctx: DeploymentPhaseContext) {
    val deployment = ctx.getDeploymentUnit

    // Check if descriptor has the right extension
    if (deployment.getAttachment(DEPLOYMENT_ROOT) != null ||
        !deployment.getName.endsWith(PlayMetadata.DESCRIPTOR_SUFFIX)) {
      debug("Not a Play application %s", deployment)
      return
    }

    val mountProvider = deployment.getAttachment(SERVER_DEPLOYMENT_REPOSITORY)
    if (mountProvider == null)
      throw new DeploymentUnitProcessingException(
        "No deployment repository available.")

    val contents = deployment.getAttachment(DEPLOYMENT_CONTENTS)
    // Internal deployments do not have any contents, so there is nothing to mount
    if (contents == null)
      return

    val name = deployment.getName
    val deploymentRoot = VFS.getChild(s"content/$name")
    var handle: Closeable = null
    try {
      handle = mountProvider.mountDeploymentContent(
          contents, deploymentRoot, MountType.REAL)
    } catch {
      case e: IOException =>
        VFSUtils.safeClose(handle)
        throw new DeploymentUnitProcessingException(
          s"Failed to mount $name file", e)
    }

    val resourceRoot = new ResourceRoot(deploymentRoot, new MountHandle(handle))
    deployment.putAttachment(DEPLOYMENT_ROOT, resourceRoot)
    deployment.putAttachment(DESCRIPTOR_ROOT, resourceRoot)
    deployment.putAttachment(MODULE_SPECIFICATION, new ModuleSpecification())
  }

  def undeploy(context: DeploymentUnit) {
    val knobRoot = context.removeAttachment(DESCRIPTOR_ROOT)
    if (knobRoot != null) {
      val mountHandle = knobRoot.getMountHandle
      VFSUtils.safeClose(mountHandle)
    }
  }

}

object PlayRootMountProcessor {

  val DESCRIPTOR_ROOT = AttachmentKey.create(classOf[ResourceRoot])

}
