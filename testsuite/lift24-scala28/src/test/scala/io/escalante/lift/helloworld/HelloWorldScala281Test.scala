package io.escalante.lift.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive

/**
 * // TODO: Merge into HelloWorldTest using:
 * https://docs.jboss.org/author/display/ARQ/Multiple+Deployments
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala281Test extends HelloWorldTest {

   // Tests in parent class

}

object HelloWorldScala281Test {

   @Deployment def deployment: WebArchive =
      HelloWorldTest.deployment(
         Some("2.4"), Some("2.8.1"), classOf[HelloWorldBoot])

}
