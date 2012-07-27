/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.arquillian

import org.jboss.arquillian.core.api.annotation.Observes
import org.jboss.arquillian.core.spi.LoadableExtension
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder
import io.escalante.lift.LiftTestSetup
import org.jboss.arquillian.container.spi.event.container._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class ArquillianTestLifecycleListener {

  def executeBeforeStart(@Observes event: BeforeStart) =
    LiftTestSetup.installExtension

  def executeAfterStop(@Observes event: AfterStop) =
    LiftTestSetup.uninstallExtension

}

class ArquillianTestLifecycleRegister extends LoadableExtension {

  def register(builder: ExtensionBuilder) {
    builder.observer(classOf[ArquillianTestLifecycleListener]);
  }

}