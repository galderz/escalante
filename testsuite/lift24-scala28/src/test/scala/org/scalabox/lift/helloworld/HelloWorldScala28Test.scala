package org.scalabox.lift.helloworld

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala28Test extends AbstractHelloWorldTest {

   // Tests in parent class

}

object HelloWorldScala28Test {

   @Deployment def deployment: WebArchive =
      AbstractHelloWorldTest.deployment(None, Some("2.8.2"))

}
