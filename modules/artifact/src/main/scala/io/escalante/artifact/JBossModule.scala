/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.artifact

import maven.MavenArtifact
import org.jboss.modules.{Module, ModuleIdentifier}
import org.jboss.as.server.deployment.module.ModuleDependency
import scala.Predef._
import io.escalante.Scala

/**
 * Metadata representation of a JBoss Module.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class JBossModule(
    val name: String,
    val export: Boolean,
    val slot: String,
    val service: Service) {

  /**
   * TODO
   *
   * @return
   */
  def moduleDependency: ModuleDependency =
    new ModuleDependency(Module.getBootModuleLoader,
      ModuleIdentifier.create(name, slot), false, export, false, false)

  /**
   * Full directory path compatible with JBoss Modules constructed
   * out of the metadata of this Maven artifact.
   */
  def moduleDirName: String = {
    new java.lang.StringBuilder()
      .append(name.replace('.', '/')).append('/')
      .append(slot).toString
  }

}

object JBossModule {

  def apply(name: String): JBossModule =
    new JBossModule(name, false, "main", NONE)

  def apply(artifact: MavenArtifact): JBossModule =
    apply(moduleName(artifact))

  def apply(scala: Scala): JBossModule = {
    val groupId = scala.groupId
    val artifactId = scala.artifactId
    apply(s"$groupId.$artifactId")
  }

  private def moduleName(artifact: MavenArtifact): String =
    new java.lang.StringBuilder()
      .append(artifact.groupId).append('.')
      .append(moduleArtifactId(artifact)).toString

  private def moduleArtifactId(artifact: MavenArtifact): String =
    artifact.artifactId.replace('.', '_')

}

sealed trait Service {def name: String}

case object NONE extends Service {val name = "none"}

case object IMPORT extends Service {val name = "import"}

case object EXPORT extends Service {val name = "export"}