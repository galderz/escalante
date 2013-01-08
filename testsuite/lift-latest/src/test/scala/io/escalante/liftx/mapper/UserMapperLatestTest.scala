/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.liftx.mapper

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import io.escalante.lift.mapper.{UserMapperScala29Test, UserMapperBoot, UserMapperTest}
import org.jboss.arquillian.container.test.api.Deployment
import org.jboss.shrinkwrap.api.spec.WebArchive

/**
 * Test Lift's mapper method for storing data with latest Lift version.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class UserMapperLatestTest extends UserMapperScala29Test {

  // Tests in parent class

}

object UserMapperLatestTest {

  @Deployment def deployment: WebArchive =
    UserMapperTest.deployment("2.9.2", "2.5-M3",
      classOf[UserMapperBoot], List(classOf[UserMapperScala29Test]))

}
