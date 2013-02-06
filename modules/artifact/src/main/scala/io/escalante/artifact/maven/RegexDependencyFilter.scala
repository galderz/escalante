/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact.maven

import org.sonatype.aether.graph.{DependencyFilter, DependencyNode}
import util.matching.Regex

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class RegexDependencyFilter extends DependencyFilter {

  lazy val regex = createRegex

  def accept(
      node: DependencyNode,
      parents: java.util.List[DependencyNode]): Boolean = {
    val dependency = node.getDependency
    if (dependency == null)
      false
    else
      regex.findFirstIn(dependency.getArtifact.getArtifactId).isDefined
  }

  def createRegex: Regex

}
