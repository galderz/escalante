/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.lift.assembly

import org.sonatype.aether.graph.{DependencyNode, DependencyFilter}

/**
 * An excluding filter for Plexus utils dependencies.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object PlexusUtilsFilter extends DependencyFilter {

  def accept(node: DependencyNode, parents: java.util.List[DependencyNode]): Boolean = {
    val dependency = node.getDependency
    dependency != null && !dependency.getArtifact
        .getArtifactId.contains("plexus-util")
  }

}
