package org.scalabox.lift.arquillian

import org.jboss.arquillian.core.api.annotation.Observes
import org.jboss.arquillian.core.spi.LoadableExtension
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder
import org.scalabox.lift.LiftTestSetup
import org.jboss.arquillian.container.spi.event.container._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class ArquillianTestLifecycleListener {

   def executeBeforeStart(@Observes event: BeforeStart) =
      LiftTestSetup.buildExtension

   def executeAfterStart(@Observes event: AfterStart) =
      LiftTestSetup.installExtension

   def executeBeforeStop(@Observes event: BeforeStop) =
      LiftTestSetup.uninstallExtension

}

class ArquillianTestLifecycleRegister extends LoadableExtension {

   def register(builder: ExtensionBuilder) {
      builder.observer(classOf[ArquillianTestLifecycleListener]);
   }

}