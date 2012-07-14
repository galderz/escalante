/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.maven

import util.matching.Regex
import org.sonatype.aether.graph.{DependencyNode, DependencyFilter}
import java.util.List

/**
 * Maven dependency filter based on regular expressions.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
abstract class RegexDependencyFilter extends DependencyFilter {

   def accept(node: DependencyNode, parents: List[DependencyNode]): Boolean = {
      val dependency = node.getDependency
      if (dependency == null)
         false
      else
         regex.findFirstIn(dependency.getArtifact.getArtifactId).isDefined
   }

   def regex: Regex

}
