package io.escalante.test.artifact.maven

import io.escalante.artifact.maven.{MavenArtifact, MavenDependencyResolver}
import org.junit.Test

/**
 * Basic test to resolve a Maven artifact.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class MavenDependencyResolverTest {

  @Test def testResolveArtifact() {
    MavenDependencyResolver.resolveArtifact(
      MavenArtifact("net.liftweb", "lift-proto_2.9.1", "2.4"))
  }

}
