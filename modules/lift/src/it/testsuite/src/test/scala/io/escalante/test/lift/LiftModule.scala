/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.test.lift

import java.io.File
import org.jboss.as.controller.Extension
import io.escalante.lift.subsystem.LiftExtension
import io.escalante.test.{BuildableModule, ModuleBuilder}
import io.escalante.xml.ScalaXmlParser._
import scala.xml.Node

/**
 * Defines how the Lift JBoss module is constructed, including the classes it
 * incorporates, the libraries it depends on...etc.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object LiftModule extends BuildableModule {

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
        </dependencies>
      </module>

    ModuleBuilder.installModule(
      archive, new File(destDir, modulePath), moduleXml)
  }

  private def injectConfiguration(config: Node): Node = {
    addExtensionSubsystem(
      <extension module="io.escalante.lift"/>,
      <subsystem xmlns="urn:escalante:lift:1.0"/>,
      config
    )
  }

  def build(destDir: File, config: Node): Node = {
    buildLiftModule(destDir)
    injectConfiguration(config)
  }

}
