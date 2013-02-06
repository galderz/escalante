/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.artifact.maven

import org.sonatype.aether.graph.DependencyFilter
import io.escalante.Scala

/**
 * Metadata representation of a Maven artifact.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class MavenArtifact(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val filter: Option[DependencyFilter]) {

  /**
   * Returns the Maven artifact's coordinates.
   */
  def coordinates: String =
    new java.lang.StringBuilder()
      .append(groupId).append(":")
      .append(artifactId).append(":")
      .append(version)
      .toString

}

object MavenArtifact {

  def apply(groupId: String, artifactId: String, version: String): MavenArtifact =
    new MavenArtifact(groupId, artifactId, version, None)

  def apply(groupId: String, artifactId: String): MavenArtifact =
    new MavenArtifact(groupId, artifactId, "", None)

  def apply(scala: Scala): MavenArtifact =
    apply(scala.groupId, scala.artifactId, scala.version)

}