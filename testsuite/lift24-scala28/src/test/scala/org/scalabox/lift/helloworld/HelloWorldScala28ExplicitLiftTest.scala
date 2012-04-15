package org.scalabox.lift.helloworld

import org.jboss.arquillian.junit.Arquillian
import org.junit.runner.RunWith
import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.Deployment

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala28ExplicitLiftTest extends AbstractHelloWorldTest {

   // Tests in parent class

}

object HelloWorldScala28ExplicitLiftTest {

   @Deployment def deployment: WebArchive =
      AbstractHelloWorldTest.deployment(Some("2.4"), Some("2.8.2"))

}
