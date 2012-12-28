/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.as.server.deployment.{Attachments, DeploymentPhaseContext, DeploymentUnit, DeploymentUnitProcessor}
import io.escalante.logging.Log
import java.io.File
import org.jboss.vfs.{VFS, VirtualFile}
import org.jboss.as.server.deployment.module._
import io.escalante.{Scala, SCALA_292}
import io.escalante.modules.{JBossModule, JBossModulesRepository}
import io.escalante.maven.MavenDependencyResolver
import org.jboss.msc.service.ServiceRegistry
import org.sonatype.aether.resolution.DependencyResolutionException

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
    val liftMetaData = deployment.getAttachment[LiftMetaData](LiftMetaData.ATTACHMENT_KEY)
    info("Metadata is: %s", liftMetaData)
    if (liftMetaData == null)
      return

    val moduleSpec = deployment.getAttachment(Attachments.MODULE_SPECIFICATION)

    val service = getLiftService(ctx.getServiceRegistry)
    val repo = new JBossModulesRepository(new File(service.thirdPartyModulesPath))

    // Attach Scala dependencies according to the Scala version passed
    liftMetaData match {
      case LiftMetaData(lift, SCALA_292, modules) =>
        moduleSpec.addSystemDependency(SCALA_MODULE_ID.moduleDependency)
      case LiftMetaData(lift, scala, modules) =>
        val module = repo.installScalaModule(scala)
        moduleSpec.addSystemDependency(module.moduleDependency)
      case _ => // TODO: Throw exception
        info("Unknown lift metadata")
        null
    }

    // Hack Lift jars into the war file cos Lift framework is currently
    // designed to be solely used by one web-app in (class loader) isolation
    liftMetaData match {
      case LiftMetaData(lift, scala, modules) =>
        val liftJars = resolveLiftJars(liftMetaData)
        addLiftJars(deployment, liftJars)
      case _ => info("Unknown Lift deployment")
    }
  }

  def undeploy(unit: DeploymentUnit) {
    // TODO: Do I need to remove the dependencies?
  }

  private def resolveLiftJars(metaData: LiftMetaData): Seq[File] = {
    // TODO: Parallelize with Scala 2.10 futures...
    // Flat map so that each maven dependencies files are then combined into
    // a single sequence of files to add to deployment unit
    metaData.mavenArtifacts.flatMap { artifact =>
      try {
        MavenDependencyResolver.resolveArtifact(artifact)
      } catch {
        case e: DependencyResolutionException =>
          // Dependency resolution failed, check if Scala version is latest
          val scala = metaData.scalaVersion
          val latestScala = Scala.main()
          if (scala.isMain) throw e
          else {
            warn("Artifact not found (exception: %s), but Scala version old (%s), " +
              "so with a more recent one: %s", e.getMessage, scala, latestScala)
            // Now try with latest Scala...
            resolveLiftJars(LiftMetaData(
              metaData.liftVersion, latestScala, metaData.modules))
          }
      }
    }.distinct // Remove duplicates to avoid duplicate mount errors
  }

  private def addLiftJars(deployment: DeploymentUnit, jars: Seq[File]) {
    val resourceRoot = deployment.getAttachment(Attachments.DEPLOYMENT_ROOT)
    val root = resourceRoot.getRoot

    jars.foreach {
      jar =>
        val temp = root.getChild("WEB-INF/lift-lib") // Virtual Lift mount point
        val repackagedJar = createZipRoot(temp, jar)
        ModuleRootMarker.mark(repackagedJar)
        deployment.addToAttachmentList(Attachments.RESOURCE_ROOTS, repackagedJar)
    }
  }

  private def createZipRoot(deploymentTemp: VirtualFile, file: File): ResourceRoot = {
    val archive = deploymentTemp.getChild(file.getName)
    val closable = VFS.mountZip(file, archive, TempFileProviderService.provider())
    new ResourceRoot(file.getName, archive, new MountHandle(closable))
  }

  private def getLiftService(registry: ServiceRegistry): LiftService = {
    val container = registry.getService(LiftService.createServiceName)
    container.getValue.asInstanceOf[LiftService]
  }

}

object LiftDependencyProcessor extends Log {

  val SCALA_MODULE_ID = new JBossModule("org.scala-lang.scala-library")

//  val JODA_TIME_MODULE_ID = new JBossModule("org.joda.time")
//
//  val SLF4J_MODULE_ID = new JBossModule("org.slf4j")
//
//  val COMMONS_CODEC_MODULE_ID = new JBossModule("org.apache.commons.codec")

}