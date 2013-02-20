/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.artifact

import java.io.File
import org.jboss.as.controller.Extension
import io.escalante.artifact.subsystem.ArtifactExtension
import io.escalante.artifact.maven.{RegexDependencyFilter, MavenArtifact}
import io.escalante.artifact.AppServerRepository
import io.escalante.{Version, Scala}
import org.sonatype.aether.graph.{DependencyNode, DependencyFilter}
import util.matching.Regex
import scala.List
import io.escalante.test.{BuildableModule, ModuleBuilder}
import scala.Some
import scala.xml.Node

/**
 * Defines how the Artifact JBoss module is constructed, including the
 * classes it incorporates, the libraries it depends on...etc.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object ArtifactModule extends BuildableModule {

  private def buildCoreModule(destDir: File) {
    val pkg = "io/escalante"
    val modulePath = s"$pkg/core/main"
    val archive = ModuleBuilder.buildJavaArchive(
      destDir, modulePath, "escalante-core.jar",
      List(s"$pkg/io", s"$pkg/xml", s"$pkg/logging", s"$pkg/yaml"),
      List(classOf[Scala], Version.getClass))

    val moduleXml =
      <module xmlns="urn:jboss:module:1.1" name="io.escalante.core">
        <resources>
          <resource-root path="escalante-core.jar"/>
        </resources>
        <dependencies>
          <module name="org.scala-lang.scala-library"/>
          <module name="javax.api"/>
          <module name="org.jboss.logging"/>
          <module name="org.yaml.snakeyaml"/>
          <module name="org.jboss.vfs"/>
        </dependencies>
      </module>

    ModuleBuilder.installModule(
        archive, new File(destDir, modulePath), moduleXml)
  }

  private def buildArtifactModule(destDir: File) {
    val modulePath = "io/escalante/artifact/main"
    val archive = ModuleBuilder.buildJavaArchive(
      destDir, modulePath, "escalante-artifact.jar",
      List("io/escalante/artifact"), List())

    archive.addAsServiceProvider(
      classOf[Extension], classOf[ArtifactExtension])

    val repo = new AppServerRepository(destDir)
    val scala = Scala()
    repo.installArtifact(MavenArtifact(scala), Some(scala.moduleXml), List())

    val subArtifacts =
      new MavenArtifact("org.apache.maven", "maven-settings", "3.0.4",
        Some(PlexusUtilsFilter)) ::
      new MavenArtifact("org.apache.maven", "maven-settings-builder", "3.0.4",
        Some(PlexusUtilsFilter)) ::
      new MavenArtifact("org.sonatype.aether", "aether-connector-wagon", "1.13.1",
        Some(ConnectorWagonDependenciesFilter)) ::
      new MavenArtifact("org.apache.maven.wagon", "wagon-http-lightweight", "1.0",
        Some(PlexusUtilsFilter)) ::
      Nil

    repo.installArtifact(new MavenArtifact("org.apache.maven",
      "maven-aether-provider", "3.0.4", Some(PlexusUtilsFilter)),
      None, subArtifacts)

    val moduleXml =
      <module xmlns="urn:jboss:module:1.1" name="io.escalante.artifact">
        <resources>
          <resource-root path="escalante-artifact.jar"/>
        </resources>
        <dependencies>
          <module name="io.escalante.core"/>
          <!-- Core dependencies -->
          <module name="javax.api"/>
          <module name="org.jboss.logging"/>
          <module name="org.scala-lang.scala-library"/>
          <!-- Artifact dependencies -->
          <module name="org.apache.maven.maven-aether-provider"/>
          <module name="org.jboss.as.controller"/>
          <module name="org.jboss.as.server"/>
          <module name="org.jboss.modules"/>
          <module name="org.jboss.staxmapper"/>
          <module name="org.jboss.vfs"/>
        </dependencies>
      </module>

    ModuleBuilder.installModule(
        archive, new File(destDir, modulePath), moduleXml)
  }

  def injectConfiguration(config: Node): Node = {
    addExtensionSubsystem(
      <extension module="io.escalante.artifact"/>,
      <subsystem xmlns="urn:escalante:artifact:1.0">
        <thirdparty-modules-repo relative-to="jboss.home.dir" path="modules"/>
      </subsystem>,
      config
    )
  }

  def build(destDir: File, config: Node): Node = {
    buildCoreModule(destDir)
    buildArtifactModule(destDir)
    injectConfiguration(config)
  }

  private object PlexusUtilsFilter extends DependencyFilter {
    def accept(
        node: DependencyNode,
        parents: java.util.List[DependencyNode]): Boolean = {
      val dependency = node.getDependency
      dependency != null && !dependency.getArtifact
          .getArtifactId.contains("plexus-util")
    }
  }

  private object ConnectorWagonDependenciesFilter extends RegexDependencyFilter {
    def createRegex: Regex = new Regex("^(?!.*(sisu|wagon-provider-api)).*$")
  }

}
