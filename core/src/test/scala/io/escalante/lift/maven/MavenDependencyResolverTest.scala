package io.escalante.lift.maven

import io.escalante.maven.{MavenDependencyResolver, MavenArtifact}
import org.junit.Test

/**
 * Basic test to resolve a Maven artifact.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class MavenDependencyResolverTest {

  @Test def testResolveArtifact {
    MavenDependencyResolver.resolveArtifact(new MavenArtifact(
      "net.liftweb", "lift-proto_2.9.1", "2.4"))
  }

}
