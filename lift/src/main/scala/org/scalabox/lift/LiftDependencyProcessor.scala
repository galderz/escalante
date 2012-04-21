package org.scalabox.lift

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import org.scalabox.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import org.jboss.modules.{Module, ModuleIdentifier}
import org.jboss.as.server.ServerEnvironment
import org.scalabox.util.SecurityActions
import maven.LiftDependencyFilter
import org.scalabox.{SCALA_282, SCALA_291}
import org.scalabox.modules.{JBossModule, JBossModulesRepository}
import org.scalabox.maven.{MavenDependencyResolver, MavenArtifact}

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

      val home = new File(SecurityActions.getSystemProperty(ServerEnvironment.HOME_DIR))
      // TODO: Why use a separate directory for downloaded modules?
      // TODO: Reason 1: Keeps downloaded vs shipped in different locations
      // TODO: Reason 2: Makes it easy to wipe out downloaded modules when tests are started
      val downloads = new File(home, "downloads")

      val repo = new JBossModulesRepository(downloads)

      liftMetaData match {
         case LiftMetaData(LIFT_24, scala) =>
            moduleSpec.addSystemDependency(createDependency(JODA_TIME_MODULE_ID))
            moduleSpec.addSystemDependency(createDependency(SLF4J_MODULE_ID))
            // Not shipped by JBoss AS, so download and install
            val module = repo.installModule(
               new MavenArtifact("commons-fileupload", "commons-fileupload", "1.2.2"),
               new JBossModule("javax.servlet.api"))
            moduleSpec.addSystemDependency(module.moduleDependency)
         case _ => info("Unknown Lift deployment")
      }

      liftMetaData match {
         case LiftMetaData(lift, SCALA_291) =>
            moduleSpec.addSystemDependency(createDependency(SCALA_MODULE_ID))
         case LiftMetaData(lift, SCALA_282) =>
            val module = repo.installModule(SCALA_282.maven)
            moduleSpec.addSystemDependency(module.moduleDependency)
         case _ => info("Unknown Lift deployment")
      }

      // Hack Lift jars into the war file cos Lift framework is currently
      // designed to be solely used by one web-app in (class loader) isolation
      liftMetaData match {
         case LiftMetaData(lift, scala) =>
            val liftJars = MavenDependencyResolver.resolveArtifact(
               new MavenArtifact("net.liftweb", "lift-mapper_" + scala.version,
                  lift.version), LiftDependencyFilter)
            addLiftJars(deployment, liftJars)
         case _ => info("Unknown Lift deployment")
      }
   }

   def undeploy(unit: DeploymentUnit) {
      // TODO: Do I need to remove the dependencies?
   }

   private def createDependency(id: ModuleIdentifier): ModuleDependency =
      new ModuleDependency(
         Module.getBootModuleLoader(), id, false, false, false)

   private def addLiftJars(deployment: DeploymentUnit, jars: Seq[File]) {
      val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val root = resourceRoot.getRoot()

      jars.foreach { jar =>
         val temp = root.getChild("WEB-INF/lift-lib") // Virtual Lift mount point
         val repackagedJar = createZipRoot(temp, jar)
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

   val JODA_TIME_MODULE_ID = ModuleIdentifier.create("org.joda.time")

   val SLF4J_MODULE_ID = ModuleIdentifier.create("org.slf4j")

   val SCALA_MODULE_ID = ModuleIdentifier.create("org.scala-lang.scala-library")

}