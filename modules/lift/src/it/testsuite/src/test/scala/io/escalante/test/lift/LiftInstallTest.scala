/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift

import org.junit.runner.RunWith
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode
import java.net.InetAddress
import io.escalante.io.Closeable._
import io.escalante.logging.Log
import io.escalante.test.AppServer

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
@RunWith(classOf[Arquillian])
class LiftInstallTest extends Log {

  @Test def testCheckInstall() {
    AppServer.assertExtensionInstalled("io.escalante.artifact")
    AppServer.assertExtensionInstalled("io.escalante.lift")
  }

}