package org.scalabox.lift.maven

import util.matching.Regex

/**
 * Maven resolution dependency filter for Lift 2.4 and Scala 2.9 applications.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object Lift24Scala29DependencyFilter extends RegexDependencyFilter {

   def regex: Regex = new Regex(
      "(lift-webkit|lift-common|lift-util|lift-json|lift-actor" +
      "|lift-proto|lift-db|lift-mapper)")

}
