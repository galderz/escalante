package org.scalabox.lift.deployment

import org.jboss.msc.service.{ServiceController, ServiceRegistry}
import org.scalabox.lift.extension.LiftService
import org.jboss.as.server.deployment._
import org.jboss.vfs.VirtualFile

/**
 * // TODO: Change name and document!
 * @author Galder Zamarreño
 * @since // TODO
 */
class SubsystemDeployment extends DeploymentUnitProcessor {

   def deploy(ctx: DeploymentPhaseContext) {
      val name = ctx.getDeploymentUnit.getName
      val service = getLiftService(ctx.getServiceRegistry, name)
      if (service != null) {
         val root = ctx.getDeploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT)
         val cool = root.getRoot.getChild("META-INF/cool.txt")
         service.addDeployment(name)
         if (cool.exists) {
            service.addCoolDeployment(name)
         }
      }
   }

   def undeploy(context: DeploymentUnit) {
      val name = context.getName
      val service = getLiftService(context.getServiceRegistry, name)
      if (service != null) {
         service.removeDeployment(name)
      }
   }

   private def getLiftService(reg: ServiceRegistry, name: String): LiftService = {
      val suffix = name.split('.').last
      val container = reg.getService(LiftService.createServiceName(suffix))
      if (container != null)
         container.getValue.asInstanceOf[LiftService]
      else
         null
   }

}

object SubsystemDeployment {

   /**
    * See {@link Phase} for a description of the different phases
    */
   val PHASE = Phase.DEPENDENCIES

   /**
    * The relative order of this processor within the {@link #PHASE}.
    * The current number is large enough for it to happen after all
    * the standard deployment unit processors that come with JBoss AS.
    */
   val PRIORITY = 0x4000

}