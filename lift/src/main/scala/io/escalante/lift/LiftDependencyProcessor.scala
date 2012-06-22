package io.escalante.lift

import maven.{Lift24Scala28DependencyFilter, Lift24Scala29DependencyFilter}
import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import org.jboss.as.server.ServerEnvironment
import io.escalante.util.SecurityActions
import io.escalante.{SCALA_282, SCALA_291}
import io.escalante.modules.{JBossModule, JBossModulesRepository}
import io.escalante.maven.{MavenDependencyResolver, MavenArtifact}

/**
 * A deployment processor that hooks the right dependencies for the Lift
 * version desired.
 *
 * @author Galder Zamarreño
 * @since 1.0
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

      // Why use a separate directory for downloaded modules?
      // Reason 1: Keeps downloaded vs shipped in different locations
      // Reason 2: Makes it easy to wipe out downloaded modules when tests are started
      val thirdPartyModulesFile = new File(home, "thirdparty-modules")

      val repo = new JBossModulesRepository(thirdPartyModulesFile)

      liftMetaData match {
         case LiftMetaData(LIFT_24, scala) =>
            moduleSpec.addSystemDependency(JODA_TIME_MODULE_ID.moduleDependency)
            moduleSpec.addSystemDependency(SLF4J_MODULE_ID.moduleDependency)
            moduleSpec.addSystemDependency(COMMONS_CODEC_MODULE_ID.moduleDependency)
            // Not shipped by JBoss AS, so download and install
            val module = repo.installModule(
               new MavenArtifact("commons-fileupload", "commons-fileupload", "1.2.2"),
               new JBossModule("javax.servlet.api"))
            moduleSpec.addSystemDependency(module.moduleDependency)
         case _ => // TODO: Throw exception
            info("Unknown Lift deployment")
      }

      val liftDependencyFilter = liftMetaData match {
         case LiftMetaData(lift, SCALA_291) =>
            moduleSpec.addSystemDependency(SCALA_MODULE_ID.moduleDependency)
            Lift24Scala29DependencyFilter
         case LiftMetaData(lift, SCALA_282) =>
            val module = repo.installScalaModule(SCALA_282)
            moduleSpec.addSystemDependency(module.moduleDependency)
            Lift24Scala28DependencyFilter
         case _ => // TODO: Throw exception
            info("Unknown Lift deployment")
            null
      }

      // Hack Lift jars into the war file cos Lift framework is currently
      // designed to be solely used by one web-app in (class loader) isolation
      liftMetaData match {
         case LiftMetaData(lift, scala) =>
            val liftJars = MavenDependencyResolver.resolveArtifact(
               new MavenArtifact("net.liftweb", "lift-mapper_" + scala.version,
                  lift.version), liftDependencyFilter)
            addLiftJars(deployment, liftJars)
         case _ => info("Unknown Lift deployment")
      }
   }

   def undeploy(unit: DeploymentUnit) {
      // TODO: Do I need to remove the dependencies?
   }

   private def addLiftJars(deployment: DeploymentUnit, jars: Seq[File]) {
      val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
      val root = resourceRoot.getRoot

      jars.foreach { jar =>
         val temp = root.getChild("WEB-INF/lift-lib") // Virtual Lift mount point
         val repackagedJar = createZipRoot(temp, jar)
         ModuleRootMarker.mark(repackagedJar)
         deployment.addToAttachmentList(Attachments.RESOURCE_ROOTS, repackagedJar)
      }
   }

   private def createZipRoot(deploymentTemp: VirtualFile, file: File): ResourceRoot = {
      val archive = deploymentTemp.getChild(file.getName);
      val closable = VFS.mountZip(file, archive, TempFileProviderService.provider());
      new ResourceRoot(file.getName, archive, new MountHandle(closable));
   }

}

object LiftDependencyProcessor extends Log {

   val SCALA_MODULE_ID = new JBossModule("org.scala-lang.scala-library")

   val JODA_TIME_MODULE_ID = new JBossModule("org.joda.time")

   val SLF4J_MODULE_ID = new JBossModule("org.slf4j")

   val COMMONS_CODEC_MODULE_ID = new JBossModule("org.apache.commons.codec")

}