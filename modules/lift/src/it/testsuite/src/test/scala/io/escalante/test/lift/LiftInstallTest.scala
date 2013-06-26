/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift

import io.escalante.logging.Log
import io.escalante.test.AppServer
import org.jboss.arquillian.junit.Arquillian
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the presence of Lift and Artifact modules installed.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
@RunWith(classOf[Arquillian])
class LiftInstallTest extends Log {

  @Test def testCheckInstall() {
    AppServer.assertExtensionInstalled("io.escalante.artifact")
    AppServer.assertExtensionInstalled("io.escalante.lift")
  }

}