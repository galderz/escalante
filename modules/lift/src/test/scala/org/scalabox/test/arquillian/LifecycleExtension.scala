package org.scalabox.test.arquillian

import org.jboss.arquillian.core.spi.LoadableExtension
import org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LifecycleExtension extends LoadableExtension {

   def register(builder: ExtensionBuilder) {
      builder.observer(classOf[LifecycleExecuter]);
   }

}