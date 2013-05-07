/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact

import maven.MavenArtifact
import scala.xml.Elem
import io.escalante.server.JBossModule
import io.escalante.Scala

/**
 * Artifact repository
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
trait ArtifactRepository {

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
   * Install Scala library artifact in the artifact repository, returning a
   * module metadata represented as an instance of JBossModule.
   *
   * @param scala Scala library to install as JBoss Module
   * @return a JBossModule representing the installed JBoss module
   */
  // Required so that slot can be correctly resolved by JBoss Module constructor
  def installArtifact(scala: Scala): JBossModule

}
