package org.scalabox.lift.resolver

import util.matching.Regex
import java.util.Collection
import org.jboss.shrinkwrap.resolver.api.maven.{MavenDependency, MavenResolutionFilter}

/**
 * Maven dependency filter based on regular expressions.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
abstract class RegexDependencyFilter extends MavenResolutionFilter {

   def accept(element: MavenDependency): Boolean =
      regex.findFirstIn(element.getCoordinates).isDefined

   def configure(dependencies: Collection[MavenDependency]): MavenResolutionFilter =
      this

   def regex: Regex

}
