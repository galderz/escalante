/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.subsystem

import org.jboss.msc.service.{ServiceName, StartContext, StopContext, Service}
import org.jboss.msc.value.InjectedValue
import org.jboss.msc.inject.Injector
import io.escalante.logging.Log
import io.escalante.{Version, Scala}
import io.escalante.artifact.{JBossModule, ArtifactRepository}
import io.escalante.artifact.maven.MavenArtifact
import org.jboss.as.server.deployment.DeploymentUnit

/**
 * The Lift module service
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class LiftService extends Service[LiftService] with Log {

  private val repository = new InjectedValue[ArtifactRepository]()

  /**
   * Starts the Lift service.
   *
   * @param context the context used to trigger service start
   */
  def start(context: StartContext) {
    info("Start Escalante Lift subsystem")
    info("Welcome to Escalante %s - http://escalante.io/", Version.VERSION)
  }

  /**
   * Stops the Lift service.
   *
   * @param context the context used to trigger service stop
   */
  def stop(context: StopContext) {
    // No-op
  }

  /**
   * Get the Lift service.
   *
   * @return the singleton instance of
   *         [[io.escalante.lift.subsystem.LiftService]]
   */
  def getValue: LiftService = this

  /**
   * Gets the artifact repository injector.
   *
   * @return a [[org.jboss.msc.inject.Injector]] instance for the artifact
   *         repository dependency
   */
  def artifactRepositoryInjector: Injector[ArtifactRepository] = repository

  /**
   * Install Scala as a JBoss Module.
   *
   * @param scala [[io.escalante.Scala]] instance representing scala version
   *             to install
   * @return a [[io.escalante.artifact.JBossModule]] representation of the
   *         installed Scala instance
   */
  def installScalaModule(scala: Scala): JBossModule = {
    repository.getValue.installArtifact(
      MavenArtifact(scala), Some(scala.moduleXml), List())
  }

  /**
   * Resolve and attach Lift jars to deployment.
   *
   * @param metaData [[io.escalante.lift.subsystem.LiftMetaData]] instance
   *                containing information on the Lift jars to resolve
   * @param deployment [[org.jboss.as.server.deployment.DeploymentUnit]]
   *                  to which Lift jars should be attached
   */
  def attachLiftJars(metaData: LiftMetaData, deployment: DeploymentUnit) {
    repository.getValue.attachArtifacts(
      metaData.mavenArtifacts, deployment, "WEB-INF/lift-lib")
  }

}

object LiftService {

  val SERVICE_NAME = ServiceName.of("escalante").append("lift")

}
