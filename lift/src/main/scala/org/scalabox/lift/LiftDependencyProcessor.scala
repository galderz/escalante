package org.scalabox.lift

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import net.liftweb.http.LiftSession
import org.jboss.modules.{Module, ModuleIdentifier}
import java.net.JarURLConnection
import net.liftweb.common.Loggable
import net.liftweb.util.HasParams
import net.liftweb.json.Printer
import net.liftweb.actor.{ILAExecute}

/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
class LiftDependencyProcessor extends DeploymentUnitProcessor {

   import LiftDependencyProcessor._

   def deploy(ctx: DeploymentPhaseContext) {
      val deployment = ctx.getDeploymentUnit
      info("Try to deploy: %s", deployment)
      val liftMetaData = deployment.getAttachment(LiftMetaData.ATTACHMENT_KEY)
      info("Metadata is: %s", liftMetaData)
      if (liftMetaData == null)
         return

      // Hack Lift jars into the war file cos Lift framework is currently
      // designed to be solely used by one web-app in (class loader) isolation
      addLiftJars(deployment)

      val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

      liftMetaData match {
         case LiftMetaData(LIFT_24, SCALA_291) =>
            moduleSpec.addSystemDependency(createDependency(JODA_TIME_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(SLF4J_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(COMMONS_FILEUPLOAD_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(SCALA_291_MODULE_ID))
         case _ => info("Unknown Lift deployment")
      }
   }

   def undeploy(unit: DeploymentUnit) {
      // TODO: Do I need to remove the dependencies?
   }

   private def createDependency(id: ModuleIdentifier): ModuleDependency =
      new ModuleDependency(
         Module.getBootModuleLoader(), id, false, false, false)

   private def addLiftJars(deployment: DeploymentUnit) {
      val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val root = resourceRoot.getRoot()

      val classes = List(
         classOf[LiftSession], // net.liftweb.lift-webkit
         classOf[Loggable], // net.liftweb.lift-common
         classOf[HasParams], // net.liftweb.lift-util
         classOf[Printer], // net.liftweb.lift-json
         classOf[ILAExecute] // net.liftweb.lift-actor
      )

      classes.foreach {
         clazz =>
         // Hack alert!!!
            val url = clazz.getProtectionDomain.getCodeSource.getLocation
            val conn = url.openConnection().asInstanceOf[JarURLConnection]
            val file = new File(conn.getJarFileURL.toURI)
            val temp = root.getChild("temp") // Virtual temp mount point
            val repackagedJar = createZipRoot(temp, file)
            ModuleRootMarker.mark(repackagedJar)
            deployment.addToAttachmentList(Attachments.RESOURCE_ROOTS, repackagedJar)
      }

   }

   private def createZipRoot(deploymentTemp: VirtualFile, file: File): ResourceRoot = {
      val archive = deploymentTemp.getChild(file.getName());
      val closable = VFS.mountZip(file, archive, TempFileProviderService.provider());
      new ResourceRoot(file.getName(), archive, new MountHandle(closable));
   }

}

object LiftDependencyProcessor extends Log {

   // TODO: Add version slot...
   val SCALA_291_MODULE_ID =
      ModuleIdentifier.create("org.scala-lang.scala-library")

   //   // TODO: Add version slot...
   //   val LIFT_24_MAPPER_MODULE_ID =
   //      ModuleIdentifier.create("net.liftweb.lift-mapper_2_9_1")

   // TODO: Add version slot...
   val LIFT_24_WEBKIT_MODULE_ID =
      ModuleIdentifier.create("net.liftweb.lift-webkit_2_9_1")

   val JODA_TIME_MODULE_ID = ModuleIdentifier.create("org.joda.time")

   val SLF4J_MODULE_ID = ModuleIdentifier.create("org.slf4j")

   val COMMONS_FILEUPLOAD_MODULE_ID =
      ModuleIdentifier.create("commons-fileupload.commons-fileupload")


   //   // TODO: Add version slot...
   //   val LIFT_24_COMMON_MODULE_ID =
   //      ModuleIdentifier.create("net.liftweb.lift-common_2_9_1")

}