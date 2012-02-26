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
class HelloWorld24Test extends AbstractHelloWorldTest {

   // Tests in parent class

}

object HelloWorld24Test {

   @Deployment def deployment: WebArchive = AbstractHelloWorldTest.deployment("2.4")

}
