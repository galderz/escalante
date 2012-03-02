package org.scalabox.lift

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.Deployment
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.scalabox.lift.helloworld.AbstractHelloWorldTest
import bootstrap.liftweb.Boot

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala29Test extends AbstractHelloWorldTest {

   // Tests in parent class

}

object HelloWorldScala29Test {

   @Deployment def deployment: WebArchive =
      AbstractHelloWorldTest.deployment(Some("2.4"), Some("2.9.1"), classOf[Boot])

}

