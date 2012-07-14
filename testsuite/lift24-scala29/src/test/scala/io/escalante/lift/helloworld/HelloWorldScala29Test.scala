/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.helloworld

import org.jboss.shrinkwrap.api.spec.WebArchive
import org.jboss.arquillian.container.test.api.{OperateOnDeployment, Deployment}
import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class HelloWorldScala29Test extends AbstractHelloWorldTest {

   @Test @OperateOnDeployment("helloworld-default")
   def testHelloWorld() {
      helloWorld("default")
   }

   @Test @OperateOnDeployment("helloworld-291")
   def testHelloWorld281() {
      helloWorld("291")
   }

   @Test @OperateOnDeployment("helloworld-290")
   def testHelloWorld280() {
      helloWorld("290")
   }

}

object HelloWorldScala29Test {

   @Deployment(name = "helloworld-default", order = 1)
   def deployment: WebArchive =
      HelloWorldTest.deployment("helloworld", "helloworld-default.war",
         Some("2.4"), None, classOf[HelloWorldBoot])

   @Deployment(name = "helloworld-291", order = 2)
   def deployment291: WebArchive = deployHelloWorld("2.9.1")

   @Deployment(name = "helloworld-290", order = 3)
   def deployment290: WebArchive = deployHelloWorld("2.9.0")

   private def deployHelloWorld(scala: String): WebArchive =
      HelloWorldTest.deployment("helloworld",
         "helloworld-%s.war".format(scala.replace(".", "")),
         Some("2.4"), Some(scala), classOf[HelloWorldBoot])

}

