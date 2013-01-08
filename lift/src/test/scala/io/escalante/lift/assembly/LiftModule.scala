/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift.assembly

import io.escalante.util.FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import java.io.{File, FileOutputStream}
import org.jboss.as.controller.Extension
import io.escalante.util.ScalaXmlParser._
import io.escalante.maven.MavenArtifact
import io.escalante.modules.JBossModulesRepository
import io.escalante.assembly.EscalanteModule
import io.escalante.lift.subsystem.LiftExtension

/**
 * Defines how the Lift JBoss module is constructed, including the classes it
 * incorporates, the libraries it depends on...etc.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftModule extends EscalanteModule {

  def build(destDir: File) {
    val modulePath = "io/escalante/lift/main"
    val moduleDir = new File(destDir, modulePath)
    val jarName = "escalante-lift.jar"

    // 1. Create directories
    mkDirs(destDir, modulePath)

    // 2. Create jar with the extension
    val archive = ShrinkWrap.create(classOf[JavaArchive], jarName)

    // TODO: Move all test resources to under io/escalante/test
    //       To make it easier to filter out test resources,
    //       particularly in testing from IDE
    archive.addPackages(true, "io/escalante")

    archive.addAsServiceProvider(classOf[Extension], classOf[LiftExtension])
    val jarInput = archive.as(classOf[ZipExporter]).exportAsInputStream()

    // 3. Install modules for Lift module dependencies
    val repo = new JBossModulesRepository(destDir)

    val subArtifacts =
      new MavenArtifact("org.apache.maven", "maven-settings", "3.0.4", PlexusUtilsFilter) ::
        new MavenArtifact("org.apache.maven", "maven-settings-builder", "3.0.4", PlexusUtilsFilter) ::
        new MavenArtifact("org.sonatype.aether", "aether-connector-wagon", "1.13.1", ConnectorWagonDependenciesFilter) ::
        new MavenArtifact("org.apache.maven.wagon", "wagon-http-lightweight", "1.0", PlexusUtilsFilter) ::
        Nil

    repo.installModule(new MavenArtifact("org.apache.maven",
      "maven-aether-provider", "3.0.4", PlexusUtilsFilter), subArtifacts)

    // 4. Generate module.xml and safe it to disk
    saveXml(new File(moduleDir, "module.xml"), moduleXml)

    // 4. Copy over the module jar
    copy(jarInput, new FileOutputStream(new File(moduleDir, jarName)))
  }

  // TODO: Revise the need to import plexus stuff now that we don't use IOC
  private def moduleXml = {
    <module xmlns="urn:jboss:module:1.1" name="io.escalante.lift">
      <resources>
        <resource-root path="escalante-lift.jar"/>
      </resources>
      <dependencies>
        <module name="javax.api"/>
        <module name="org.jboss.staxmapper"/>
        <module name="org.jboss.as.controller"/>
        <module name="org.jboss.as.server"/>
        <module name="org.jboss.as.ee"/>
        <module name="org.jboss.as.web"/>
        <module name="org.jboss.metadata"/>
        <module name="org.jboss.modules"/>
        <module name="org.jboss.msc"/>
        <module name="org.jboss.logging"/>
        <module name="org.jboss.vfs"/>
        <module name="org.scala-lang.scala-library"/>
        <module name="org.yaml.snakeyaml"/>
        <!-- Optional dependencies for JPA -->
        <module name="org.apache.xerces" optional="true"/>
        <module name="org.apache.maven.maven-aether-provider"
                services="import">
          <imports>
            <include-set>
              <path name="META-INF/plexus"/>
            </include-set>
          </imports>
        </module>
      </dependencies>
    </module>
  }

}
