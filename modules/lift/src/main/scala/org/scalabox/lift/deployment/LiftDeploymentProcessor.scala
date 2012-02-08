package org.scalabox.lift.deployment

import org.scalabox.lift.extension.LiftMetaData
import org.scalabox.logging.Log
import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import net.liftweb.http.Bootable
import net.liftweb.util.ClassHelpers._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
// TODO: Remove this!
class LiftDeploymentProcessor extends DeploymentUnitProcessor {

   import LiftDeploymentProcessor._

   def deploy(ctx: DeploymentPhaseContext) {
      val deployment = ctx.getDeploymentUnit
      val liftMetaData = deployment.getAttachment(LiftMetaData.ATTACHMENT_KEY)
      info("Metadata is: %s", liftMetaData)
      if (liftMetaData == null)
         return
   }

   def undeploy(context: DeploymentUnit) {
   }

}

object LiftDeploymentProcessor extends Log {

   // Empty

}

//class LiftBootFinder extends Bootable {
//
//   def boot() {
//      // TODO: Do I really need all this? Do I really need ClassHelpers?
//      val f = createInvoker("boot",
//         Class.forName("bootstrap.liftweb.Boot", false, Thread.currentThread().getContextClassLoader)
//               .newInstance.asInstanceOf[AnyRef])
//      f.map {f => f()}
//   }
//
//}
