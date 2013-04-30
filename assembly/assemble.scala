/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import io.escalante.test.AppServer
import io.escalante.test.artifact.ArtifactModule
import io.escalante.test.lift.LiftModule
import io.escalante.test.play.PlayModule
import java.io.File
import io.escalante.io.FileSystem._

// Assemble Escalante
val baseDir = project.getBasedir.getCanonicalPath
val jbossVersion = project.getProperties.getProperty("version.jboss.as")
val scalaVersion = project.getProperties.getProperty("version.scala")
val projectVersion = project.getVersion
val escalantePrefix = "escalante-"

val targetDir = s"$baseDir/target"

// 0. Log startup
println()
println(s"""|-------------------
           | Assemble Escalante
           |-------------------
           | baseDir = $baseDir
           | jbossVersion = $jbossVersion
           | scalaVersion = $scalaVersion
         """.stripMargin)

// 1. Extract JBoss AS distro, if necessary...
val escalanteDirName = escalantePrefix + projectVersion
val escalanteHome = new File(s"$targetDir/$escalanteDirName")
AppServer.unzipAppServer(escalanteHome, jbossVersion)

// 2. Build Escalante, reusing the code used to unit test Escalante (how cool!!!)
println("Build modules and apply XML configuration changes")
AppServer.distSetUp(escalanteHome, List(ArtifactModule, LiftModule, PlayModule))

// 3. Copy xsd files
println("Copy susystem XML schema files")
for (module <- List("lift", "artifact"))
yield {
  val canonicalPath = escalanteHome.getCanonicalPath
  copy(s"$baseDir/../modules/$module/target/classes/schema",
    s"$canonicalPath/docs/schema")
}

println("Escalante assembled")

















