package io.escalante.lift.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.Deployment
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian

/**
 * // TODO: Merge into HelloWorldTest using:
 * https://docs.jboss.org/author/display/ARQ/Multiple+Deployments
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala290Test extends HelloWorldTest {

   // Tests in parent class

}

object HelloWorldScala290Test {

   @Deployment def deployment: WebArchive =
      HelloWorldTest.deployment(
         Some("2.4"), Some("2.9.0"), classOf[HelloWorldBoot])

}

