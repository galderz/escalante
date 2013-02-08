/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift

import io.escalante.io.FileSystem
import FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import java.io.{File, FileOutputStream}
import org.jboss.as.controller.Extension
import io.escalante.xml.ScalaXmlParser._
import io.escalante.lift.subsystem.LiftExtension
import io.escalante.artifact.maven.MavenArtifact
import io.escalante.artifact.AppServerRepository
import io.escalante.Scala
import io.escalante.test.artifact.ArtifactModule
import io.escalante.test.ModuleBuilder

/**
 * Defines how the Lift JBoss module is constructed, including the classes it
 * incorporates, the libraries it depends on...etc.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftModule {

  private def buildLiftModule(destDir: File) {
    val modulePath = "io/escalante/lift/main"
    val archive = ModuleBuilder.buildJavaArchive(
      destDir, modulePath, "escalante-lift.jar",
      List("io/escalante/lift"), List())

    archive.addAsServiceProvider(
      classOf[Extension], classOf[LiftExtension])

    val moduleXml =
      <module xmlns="urn:jboss:module:1.1" name="io.escalante.lift">
        <resources>
          <resource-root path="escalante-lift.jar"/>
        </resources>
        <dependencies>
          <module name="io.escalante.core"/>
          <module name="io.escalante.artifact"/>
          <!-- Core dependencies -->
          <module name="org.scala-lang.scala-library"/>
          <module name="javax.api"/>
          <module name="org.jboss.logging"/>
          <!-- Artifact dependencies -->
          <module name="org.jboss.as.controller"/>
          <module name="org.jboss.as.server"/>
          <module name="org.jboss.staxmapper"/>
          <module name="org.apache.maven.maven-aether-provider"/>
          <!-- Lift dependencies -->
          <module name="org.jboss.as.ee"/>
          <module name="org.jboss.vfs"/>
          <module name="org.jboss.metadata"/>
          <module name="org.jboss.as.web"/>

          <!--
          <module name="org.jboss.msc"/>
          Optional dependencies for JPA
          <module name="org.apache.xerces" optional="true"/>
          <module name="org.apache.maven.maven-aether-provider"
                  services="import">
            <imports>
              <include-set>
                <path name="META-INF/plexus"/>
              </include-set>
            </imports>
          </module>
           -->
        </dependencies>
      </module>

    ModuleBuilder.installModule(
      archive, new File(destDir, modulePath), moduleXml)
  }

  def build(destDir: File) {
    buildLiftModule(destDir)

//    val modulePath = "io/escalante/lift/main"
//    val moduleDir = new File(destDir, modulePath)
//    val jarName = "escalante-lift.jar"
//
//    // 1. Create directories
//    mkDirs(destDir, modulePath)
//
//    // 2. Create jar with the extension
//    val archive = ShrinkWrap.create(classOf[JavaArchive], jarName)
//
//    // TODO: Move all test resources to under io/escalante/test
//    //       To make it easier to filter out test resources,
//    //       particularly in testing from IDE
//    archive.addPackages(true, "io/escalante")
//
//    archive.addAsServiceProvider(classOf[Extension], classOf[LiftExtension])
//    val jarInput = archive.as(classOf[ZipExporter]).exportAsInputStream()
//
//    // 3. Install modules for Lift module dependencies
//    val repo = new AppServerRepository(destDir)
//
//    // Install main Scala module
//    repo.installArtifact(MavenArtifact(Scala()), None, List())
//
//    val subArtifacts =
//      new MavenArtifact("org.apache.maven", "maven-settings", "3.0.4",
//        Some(PlexusUtilsFilter)) ::
//      new MavenArtifact("org.apache.maven", "maven-settings-builder", "3.0.4",
//        Some(PlexusUtilsFilter)) ::
//      new MavenArtifact("org.sonatype.aether", "aether-connector-wagon", "1.13.1",
//        Some(ConnectorWagonDependenciesFilter)) ::
//      new MavenArtifact("org.apache.maven.wagon", "wagon-http-lightweight", "1.0",
//        Some(PlexusUtilsFilter)) ::
//      Nil
//
//    repo.installArtifact(new MavenArtifact("org.apache.maven",
//      "maven-aether-provider", "3.0.4", Some(PlexusUtilsFilter)),
//      None, subArtifacts)
//
//    // 4. Generate module.xml and safe it to disk
//    saveXml(new File(moduleDir, "module.xml"), moduleXml)
//
//    // 4. Copy over the module jar
//    copy(jarInput, new FileOutputStream(new File(moduleDir, jarName)))
  }

//  // TODO: Revise the need to import plexus stuff now that we don't use IOC
//  private def moduleXml = {
//    <module xmlns="urn:jboss:module:1.1" name="io.escalante.lift">
//      <resources>
//        <resource-root path="escalante-lift.jar"/>
//      </resources>
//      <dependencies>
//        <module name="javax.api"/>
//        <module name="org.jboss.staxmapper"/>
//        <module name="org.jboss.as.controller"/>
//        <module name="org.jboss.as.server"/>
//        <module name="org.jboss.as.ee"/>
//        <module name="org.jboss.as.web"/>
//        <module name="org.jboss.metadata"/>
//        <module name="org.jboss.modules"/>
//        <module name="org.jboss.msc"/>
//        <module name="org.jboss.logging"/>
//        <module name="org.jboss.vfs"/>
//        <module name="org.scala-lang.scala-library"/>
//        <module name="org.yaml.snakeyaml"/>
//        <!-- Optional dependencies for JPA -->
//        <module name="org.apache.xerces" optional="true"/>
//        <module name="org.apache.maven.maven-aether-provider"
//                services="import">
//          <imports>
//            <include-set>
//              <path name="META-INF/plexus"/>
//            </include-set>
//          </imports>
//        </module>
//      </dependencies>
//    </module>
//  }

}
