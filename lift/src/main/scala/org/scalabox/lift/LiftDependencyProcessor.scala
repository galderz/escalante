package org.scalabox.lift

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import org.jboss.modules.{Module, ModuleIdentifier}
import java.net.URL

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

      val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

      // TODO: Make it more sane...

      liftMetaData match {
         case LiftMetaData(LIFT_24, scala) =>
            moduleSpec.addSystemDependency(createDependency(JODA_TIME_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(SLF4J_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(COMMONS_FILEUPLOAD_MODULE_ID))
         case _ => info("Unknown Lift deployment")
      }

      liftMetaData match {
         case LiftMetaData(lift, SCALA_291) =>
            moduleSpec.addSystemDependency(createDependency(SCALA_291_MODULE_ID))
         case LiftMetaData(lift, SCALA_282) =>
            moduleSpec.addSystemDependency(createDependency(SCALA_282_MODULE_ID))
         case _ => info("Unknown Lift deployment")
      }

      // Hack Lift jars into the war file cos Lift framework is currently
      // designed to be solely used by one web-app in (class loader) isolation
      liftMetaData match {
         case LiftMetaData(LIFT_24, SCALA_291) =>
            addLiftJars(deployment, List(
               "net/liftweb/lift-webkit_2_9_1/main/lift-webkit_2.9.1-2.4.jar",
               "net/liftweb/lift-common_2_9_1/main/lift-common_2.9.1-2.4.jar",
               "net/liftweb/lift-util_2_9_1/main/lift-util_2.9.1-2.4.jar",
               "net/liftweb/lift-json_2_9_1/main/lift-json_2.9.1-2.4.jar",
               "net/liftweb/lift-actor_2_9_1/main/lift-actor_2.9.1-2.4.jar"
            ))
         case LiftMetaData(LIFT_24, SCALA_282) =>
            addLiftJars(deployment, List(
               "net/liftweb/lift-webkit_2_8_2/main/lift-webkit_2.8.2-2.4.jar",
               "net/liftweb/lift-common_2_8_2/main/lift-common_2.8.2-2.4.jar",
               "net/liftweb/lift-util_2_8_2/main/lift-util_2.8.2-2.4.jar",
               "net/liftweb/lift-json_2_8_2/main/lift-json_2.8.2-2.4.jar",
               "net/liftweb/lift-actor_2_8_2/main/lift-actor_2.8.2-2.4.jar"
            ))
         case _ => info("Unknown Lift deployment")
      }
   }

   def undeploy(unit: DeploymentUnit) {
      // TODO: Do I need to remove the dependencies?
   }

   private def createDependency(id: ModuleIdentifier): ModuleDependency =
      new ModuleDependency(
         Module.getBootModuleLoader(), id, false, false, false)

   private def addLiftJars(deployment: DeploymentUnit, jarPaths: List[String]) {
      val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val root = resourceRoot.getRoot()

      val thisUrl = classOf[LiftDependencyProcessor].getProtectionDomain.getCodeSource.getLocation
      val thisUrlElems = thisUrl.getPath.split(System.getProperty("file.separator"))
      val rootPath = thisUrlElems.take(thisUrlElems.length - 5).mkString(System.getProperty("file.separator"))

      jarPaths.foreach { jarPath =>
         val url = new URL("%s/%s".format(rootPath, jarPath))
         val file = new File(url.toURI)
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

   val SCALA_291_MODULE_ID =
      ModuleIdentifier.create("org.scala-lang.scala-library")

   val SCALA_282_MODULE_ID =
      ModuleIdentifier.create("org.scala-lang.scala-library", "2.8.2")

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