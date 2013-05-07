/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.server

import java.io.File
import io.escalante.artifact.maven.{MavenArtifact, MavenDependencyResolver}
import io.escalante.io.FileSystem._
import io.escalante.xml.ScalaXmlParser._
import scala.xml.Elem
import io.escalante.logging.Log
import io.escalante.artifact.ArtifactRepository
import scala.Some
import io.escalante.util.matching.RegularExpressions
import io.escalante.Scala

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class AppServerRepository(root: File) extends ArtifactRepository with Log {

  override def installArtifact(
      artifact: MavenArtifact,
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact]): JBossModule =
    installArtifact(JBossModule(artifact), artifact, None, dependencies, subArtifacts)

  override def installArtifact(scala: Scala): JBossModule =
    installArtifact(JBossModule(scala), MavenArtifact(scala), Some(scala.moduleXml), List(), List())

  private def installArtifact(
      module: JBossModule,
      artifact: MavenArtifact,
      moduleXml: Option[Elem],
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact]): JBossModule = {
    // Get module directory and verify if Maven resolution is required
    val moduleDir = new File(root, module.moduleDirName)
    if (requiresMavenResolution(moduleDir)) {
      val dir = mkDirs(root, module.moduleDirName)

      // TODO: Parallelize with Scala 2.10 futures...

      // Take all artifacts, both main artifact and sub-artifact,
      // and create a single list will all the jar files
      val jarFiles = (artifact :: subArtifacts.toList).flatMap(artifact =>
        MavenDependencyResolver.resolveArtifact(artifact))

      jarFiles.foreach(jar => copy(jar, new File(dir, jar.getName)))

      val descriptor = moduleXml.getOrElse {
        val templateModuleXml =
          <module xmlns="urn:jboss:module:1.1" name={module.name}
                  slot={module.slot}>
            <resources/>
            <dependencies/>
          </module>

        val resourceRoots =
          for (jar <- jarFiles) yield <resource-root path={jar.getName}/>

        val withResourceRoots =
          addXmlElements("resources", resourceRoots.toSeq, templateModuleXml)

        val moduleDeps =
          for (dep <- dependencies) yield <module name={dep.name}/>

        addXmlElements("dependencies", moduleDeps.toSeq, withResourceRoots)
      }

      saveXml(new File(dir, "module.xml"), descriptor)
    }

    module
  }

  /**
   * TODO
   *
   * @param moduleDir
   * @return
   */
  private def requiresMavenResolution(moduleDir: File): Boolean = {
    !moduleDir.exists() ||
        findFirst(moduleDir, RegularExpressions.JarFileRegex).isEmpty
  }

}
