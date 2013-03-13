/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.server

import java.io.File
import org.jboss.as.server.deployment.DeploymentUnit
import io.escalante.artifact.maven.{MavenArtifact, MavenDependencyResolver}
import io.escalante.io.FileSystem._
import io.escalante.xml.ScalaXmlParser._
import scala.xml.Elem
import io.escalante.logging.Log
import io.escalante.artifact.ArtifactRepository
import scala.Some
import io.escalante.util.matching.RegularExpressions

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class AppServerRepository(root: File) extends ArtifactRepository with Log {

  def installArtifact(
      artifact: MavenArtifact,
      moduleXml: Elem): JBossModule =
    installArtifact(artifact, Some(moduleXml), List(), List())

  def installArtifact(
      artifact: MavenArtifact,
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact]): JBossModule =
    installArtifact(artifact, None, dependencies, subArtifacts)

  def attachArtifacts(
      artifacts: Seq[MavenArtifact],
      deployment: DeploymentUnit,
      mountPoint: String) {
    // TODO: Parallelize with Scala 2.10 futures...
    // Flat map so that each maven dependencies files are then combined into
    // a single sequence of files to add to deployment unit
    val jars = artifacts.flatMap { artifact =>
      MavenDependencyResolver.resolveArtifact(artifact)
      // TODO: add more clever logic to resolveArtifact:
      //  - if lift 2.4 + scala 2.9.2 does not exist, check is scala version is latest
      //  - if it is, try "decreasing version", so "2.9.1"... that way all the way down
      //  - if it's not latest, try latest and then others
    }.distinct // Remove duplicates to avoid duplicate mount errors

    Deployments.attachTo(deployment, mountPoint, jars :_*)
  }

  private def installArtifact(
      artifact: MavenArtifact,
      moduleXml: Option[Elem],
      dependencies: Seq[JBossModule],
      subArtifacts: Seq[MavenArtifact]): JBossModule = {
    // Start by creating a JBoss module out of the Maven artifact
    val module = JBossModule(artifact)
    val moduleDir = new File(root, module.moduleDirName)
    // Check if maven resolution necessary
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
