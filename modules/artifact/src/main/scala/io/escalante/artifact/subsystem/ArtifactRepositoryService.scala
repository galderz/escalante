/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact.subsystem

import org.jboss.msc.service.{ServiceName, StartContext, StopContext, Service}
import io.escalante.logging.Log
import io.escalante.artifact.ArtifactRepository
import io.escalante.artifact.maven.MavenArtifact
import scala.xml.Elem
import org.jboss.msc.value.InjectedValue
import org.jboss.as.controller.services.path.PathManager
import org.jboss.msc.inject.Injector
import java.io.File
import io.escalante.server.{JBossModule, AppServerRepository}
import io.escalante.Scala

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class ArtifactRepositoryService(
    modulesRelativeTo: Option[String],
    modulesPath: String)
    extends Service[ArtifactRepositoryService]
    with Log
    with ArtifactRepository {

  private val pathManager = new InjectedValue[PathManager]()

  private var repository: ArtifactRepository = _

  var thirdPartyModulesPath: String = _

  /**
   * Get the artifact repository service.
   *
   * @return this instance
   */
  override def getValue: ArtifactRepositoryService = this

  /**
   * Get the path manager injector.
   *
   * @return an [[org.jboss.msc.inject.Injector]] instance for
   *         [[org.jboss.as.controller.services.path.PathManager]]
   */
  def pathManagerInjector: Injector[PathManager] = pathManager

  /**
   * Starts the artifact repository service.
   *
   * @param context the context used to trigger service start
   */
  override def start(context: StartContext) {
    thirdPartyModulesPath = pathManager.getValue
        .resolveRelativePathEntry(modulesPath, modulesRelativeTo.getOrElse(null))

    repository = new AppServerRepository(new File(thirdPartyModulesPath))

    info("Start Escalante Artifact subsystem")
  }

  /**
   * Stops the artifact repository service.
   *
   * @param context the context used to trigger service stop
   */
  override def stop(context: StopContext) {
    repository = null
  }

  override def installArtifact(
      artifact: MavenArtifact,
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact]): JBossModule =
    repository.installArtifact(artifact, dependencies, subArtifacts)

  override def installArtifact(scala: Scala): JBossModule =
    repository.installArtifact(scala)

}

object ArtifactRepositoryService {

  val SERVICE_NAME = ServiceName.of("escalante").append("artifact")

}