/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.liftx.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.{OperateOnDeployment, Deployment}
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import io.escalante.lift.helloworld.HelloWorldScala29Test

/**
 * Hello world test with latest Lift version.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class HelloWorldLatestTest extends HelloWorldScala29Test {

  // Tests are in super class

}

object HelloWorldLatestTest {

  @Deployment(name = "helloworld-default", order = 1, testable = false)
  def deployment: WebArchive =
      HelloWorldScala29Test.deployHelloWorld(Some("2.5-M3"), None)

  @Deployment(name = "helloworld-291", order = 2, testable = false)
  def deployment291: WebArchive =
      HelloWorldScala29Test.deployHelloWorld(Some("2.5-M3"), Some("2.9.1"))

  @Deployment(name = "helloworld-290", order = 3, testable = false)
  def deployment290: WebArchive =
    HelloWorldScala29Test.deployHelloWorld(Some("2.5-M3"), Some("2.9.0"))

}

