package org.scalabox.test.arquillian

import _root_.java.lang.reflect.Method
import org.jboss.arquillian.core.api.annotation.Observes
import org.jboss.arquillian.test.spi.TestClass
import org.jboss.arquillian.container.spi.event.container.{BeforeStart, AfterUnDeploy, BeforeDeploy}
import org.scalabox.test.AbstractLiftTest

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class LifecycleExecuter {

   def executeBeforeStart(@Observes event: BeforeStart) {
      // TODO: Only parameter allowed at this stage, check with Andrew/Aslak
      AbstractLiftTest.buildExtension
//      execute(testClass.getMethods(
//         classOf[org.scalabox.test.arquillian.java.BeforeStart]))
   }

   def executeBeforeDeploy(@Observes event: BeforeDeploy, testClass: TestClass) {
      AbstractLiftTest.installExtension
//      execute(testClass.getMethods(
//         classOf[org.scalabox.test.arquillian.java.BeforeDeploy]))
   }

   def executeAfterUnDeploy(@Observes event: AfterUnDeploy , testClass: TestClass) {
      AbstractLiftTest.uninstallExtension
//      execute(testClass.getMethods(
//         classOf[org.scalabox.test.arquillian.java.AfterUndeploy]))
   }

   private def execute(methods: Array[Method]) {
      if(methods == null)
         return;

      for(method <- methods) {
         try {
            method.invoke(null);
         } catch {
            case e: Exception => throw new RuntimeException(
               "Could not execute @BeforeDeploy method: " + method, e)
         }
      }
   }

}