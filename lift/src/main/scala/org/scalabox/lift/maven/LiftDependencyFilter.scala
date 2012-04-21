package org.scalabox.lift.maven

import util.matching.Regex

/**
 * Maven resolution dependency filter for Lift applications.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftDependencyFilter extends RegexDependencyFilter {

   def regex: Regex = new Regex(
      "(lift-webkit|lift-common|lift-util|lift-json|lift-actor)")

}
