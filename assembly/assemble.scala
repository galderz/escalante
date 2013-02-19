/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import io.escalante.test.AppServer
import io.escalante.test.artifact.ArtifactModule
import io.escalante.test.lift.LiftModule
import java.io.{FilenameFilter, File}
import io.escalante.io.FileSystem._

// Assemble Escalante
val baseDir = project.getBasedir.getCanonicalPath
val jbossVersion = project.getProperties.getProperty("version.jboss.as")
val scalaVersion = project.getProperties.getProperty("version.scala")
val projectVersion = project.getVersion
val escalantePrefix = "escalante-"

val targetDir = "%s/target".format(baseDir)

// 0. Log startup
println()
println( """|-------------------
           | Assemble Escalante
           |-------------------
           | baseDir = %s
           | jbossVersion = %s
           | scalaVersion = %s
         """.format(baseDir, jbossVersion, scalaVersion).stripMargin)

// 1. Extract JBoss AS distro, if necessary...
val escalanteHome = new File(
  "%s/%s".format(targetDir, escalantePrefix + projectVersion))
AppServer.unzipAppServer(escalanteHome, jbossVersion)

// 2. Build Escalante, reusing the code used to unit test Escalante (how cool!!!)
println("Build modules and apply XML configuration changes")
AppServer.distSetUp(escalanteHome, List(ArtifactModule, LiftModule))

// 3. Copy xsd files
println("Copy susystem XML schema files")
for (module <- List("lift", "artifact"))
yield
  copy("%s/../modules/%s/target/classes/schema".format(baseDir, module),
     "%s/docs/schema".format(escalanteHome.getCanonicalPath))

println("Escalante assembled")

















