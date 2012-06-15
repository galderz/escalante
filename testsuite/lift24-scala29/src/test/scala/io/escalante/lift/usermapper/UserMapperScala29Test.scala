package io.escalante.lift.usermapper

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
class UserMapperScala29Test extends UserMapperTest {

   // Tests in parent class

}

object UserMapperScala29Test {

   @Deployment def deployment: WebArchive =
      UserMapperTest.deployment(
         Some("2.4"), Some("2.9.1"), classOf[UserMapperBoot])

}
