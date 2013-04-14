/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.play

import java.io.File
import org.jboss.as.controller.Extension
import io.escalante.test.{BuildableModule, ModuleBuilder}
import scala.xml.Node
import io.escalante.play.subsystem.PlayExtension
import io.escalante.artifact.maven.{RegexDependencyFilter, MavenArtifact}
import scala.util.matching.Regex
import io.escalante.server.{JBossModule, AppServerRepository}
import io.escalante.util.matching.RegularExpressions

/**
 * Defines how the Play JBoss module is constructed, including the classes it
 * incorporates, the libraries it depends on...etc.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object PlayModule extends BuildableModule {

  private def buildPlayModule(destDir: File) {
    val modulePath = "io/escalante/play/main"
    val archive = ModuleBuilder.buildJavaArchive(
      destDir, modulePath, "escalante-play.jar",
      List("io/escalante/play", "play/core/server"), List())

    archive.addAsServiceProvider(
      classOf[Extension], classOf[PlayExtension])

    // TODO: Installing a particular Play version to be removed
    // Once compilation on the fly works (see SBT compiler for IntelliJ
    // Scala integration), Play SPIs will be compiled at deploy time based on
    // Play version used by the application
    val repo = new AppServerRepository(destDir)
    val playArtifact = new MavenArtifact(
      "play", "play_2.10", "2.1.0", Some(ScalaLibraryFilter))
    val playModuleDeps = List(
        JBossModule("org.scala-lang.scala-library"),
        JBossModule("javax.api"),
        JBossModule("sun.jdk"),
        JBossModule("org.slf4j"),
        JBossModule("org.jboss.logging.jul-to-slf4j-stub")
        )
    repo.installArtifact(playArtifact, playModuleDeps, List())

    val moduleXml =
      <module xmlns="urn:jboss:module:1.1" name="io.escalante.play">
        <resources>
          <resource-root path="escalante-play.jar"/>
        </resources>
        <dependencies>
          <module name="io.escalante.core"/>
          <module name="io.escalante.artifact"/>
          <!-- Core dependencies -->
          <module name="org.scala-lang.scala-library"/>
          <module name="javax.api"/>
          <module name="sun.jdk"/>
          <module name="org.jboss.logging"/>
          <!-- Artifact dependencies -->
          <module name="org.jboss.as.controller"/>
          <module name="org.jboss.as.server"/>
          <module name="org.jboss.staxmapper"/>
          <module name="org.apache.maven.maven-aether-provider"/>
          <!-- Play dependencies -->
          <module name="org.jboss.vfs"/>
          <module name="play.play_2_10"/>
        </dependencies>
      </module>

    ModuleBuilder.installModule(
      archive, new File(destDir, modulePath), moduleXml)
  }

  private def injectConfiguration(config: Node): Node = {
    addExtensionSubsystem(
      <extension module="io.escalante.play"/>,
      <subsystem xmlns="urn:escalante:play:1.0"/>,
      config
    )
  }

  def build(destDir: File, config: Node): Node = {
    buildPlayModule(destDir)
    injectConfiguration(config)
  }

  private object ScalaLibraryFilter extends RegexDependencyFilter {
    def createRegex: Regex = RegularExpressions.NotProvidedByServerRegex
  }

}
