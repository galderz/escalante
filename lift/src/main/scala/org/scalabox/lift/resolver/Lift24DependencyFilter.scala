package org.scalabox.lift.resolver

import util.matching.Regex

/**
 * Maven resolution dependency filter for Lift 2.4 applications.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
object Lift24DependencyFilter extends RegexDependencyFilter {

   def regex: Regex = new Regex(
      "(lift-webkit|lift-common|lift-util|lift-json|lift-actor)")

}
