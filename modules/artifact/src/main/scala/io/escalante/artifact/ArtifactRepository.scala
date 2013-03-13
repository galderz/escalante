/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact

import maven.MavenArtifact
import org.jboss.as.server.deployment.DeploymentUnit
import scala.xml.Elem
import io.escalante.server.JBossModule

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
trait ArtifactRepository {

  /**
   * resolve and add as attachment to deployment unit in mount point
   */
  def attachArtifacts(
      artifacts: Seq[MavenArtifact],
      deployment: DeploymentUnit,
      mountPoint: String)

  /**
   * Install a Maven artifact in the artifact repository, returning a module
   * metadata represented as an instance of JBossModule.
   *
   * @param artifact maven artifact to install as JBoss Module
   * @param dependencies optional collection of JBoss modules on which
   *                     this artifact depends, typically used to hook
   *                     existing modules into an artifact
   * @param subArtifacts optional collection of maven artifacts that need to
   *                     be installed within the same module
   * @return a JBossModule representing the installed JBoss module
   */
  def installArtifact(
      artifact: MavenArtifact,
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact] = List()): JBossModule

  /**
   * Install a Maven artifact in the artifact repository, returning a module
   * metadata represented as an instance of JBossModule.
   *
   * @param artifact maven artifact to install as JBoss Module
   * @param moduleXml module.xml for this JBoss module
   * @return a JBossModule representing the installed JBoss module
   */
  def installArtifact(
      artifact: MavenArtifact,
      moduleXml: Elem): JBossModule

}
