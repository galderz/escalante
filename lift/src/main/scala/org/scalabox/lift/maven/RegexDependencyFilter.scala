package org.scalabox.lift.maven

import util.matching.Regex
import org.sonatype.aether.graph.{DependencyNode, DependencyFilter}
import java.util.{List, Collection}

/**
 * Maven dependency filter based on regular expressions.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
abstract class RegexDependencyFilter extends DependencyFilter {

   def accept(node: DependencyNode, parents: List[DependencyNode]): Boolean =
      regex.findFirstIn(node.getDependency.getArtifact.getArtifactId).isDefined

   def regex: Regex

}
