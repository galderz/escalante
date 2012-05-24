package org.scalabox.lift.maven

import org.scalabox.maven.{MavenDependencyResolver, MavenArtifact}
import org.junit.Test

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@Test
class MavenDependencyResolverTest {

   def testResolveArtifact {
      MavenDependencyResolver.resolveArtifact(new MavenArtifact(
         "net.liftweb", "lift-proto_2.9.1", "2.4"))
   }

}
