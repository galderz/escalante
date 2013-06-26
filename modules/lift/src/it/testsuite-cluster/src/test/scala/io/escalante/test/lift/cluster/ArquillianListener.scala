/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift.cluster

import io.escalante.logging.Log
import io.escalante.test.AppServer
import io.escalante.test.artifact.ArtifactModule
import io.escalante.test.lift.LiftModule
import org.jboss.arquillian.container.spi.event.container._
import org.jboss.arquillian.core.api.annotation.Observes
import org.jboss.arquillian.core.spi.LoadableExtension
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder

/**
 * Arquillian test lifecycle implementation that installs the Lift extension
 * before the container has started, and cleans up after the container has
 * stopped.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class ArquillianListener extends LoadableExtension with Log {

  /**
   * Registers Arquillian test lifecycle implementation
   * that deals with set up and tear down of extension.
   */
  def register(builder: ExtensionBuilder) {
    builder.observer(classOf[ArquillianListener])
  }

  def executeBeforeSetup(@Observes event: BeforeSetup) {
    info("Execute BeforeSetup for Lift")
    AppServer.setUpAppServerCluster()
  }

  def executeBeforeStart(@Observes event: BeforeStart) {
    info("Execute beforeStart for Lift")
    AppServer.setUpModulesCluster(List(ArtifactModule, LiftModule))
  }

  def executeAfterStop(@Observes event: AfterStop) {
    info("Execute afterStop for Lift")
    AppServer.tearDownAppServerCluster()
  }

}
