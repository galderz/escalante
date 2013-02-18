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
val jbossPrefix = "jboss-as-"
val escalantePrefix = "escalante-"
val userHome = System.getProperty("user.home")

val targetDir = "%s/target".format(baseDir)
val m2Repo = "%s/.m2/repository".format(userHome)
val jbossZip = "%1$s/org/jboss/as/jboss-as-dist/%2$s/jboss-as-dist-%2$s.zip"
  .format(m2Repo, jbossVersion)
val jbossTarget = "%s/%s%s".format(targetDir, jbossPrefix, jbossVersion)

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
println("Extract JBoss Application Server distribution")
val target = new File(targetDir)
val escalanteTarget = new File(
  "%s/%s".format(targetDir, escalantePrefix + projectVersion))
val escalanteDirs = target.listFiles(new FilenameFilter {
  def accept(dir: File, name: String) = name.startsWith(escalantePrefix)
})
if (escalanteDirs == null) error("Base dir %s does not exist!".format(baseDir))
if (escalanteDirs.length > 0) {
  println("Base JBoss AS distribution already extracted")
} else {
  println("Unzip base JBoss AS distribution to %s".format(target.getCanonicalPath))
  unzip(new File(jbossZip), target)
  // Change permissions of .sh files
  val executables = new File("%s/bin".format(jbossTarget)).listFiles(
    new FilenameFilter {
      def accept(dir: File, name: String) = name.endsWith(".sh")
    })
  executables.foreach(_.setExecutable(true))
  // 2. Rename to Escalante with version
  val renamed = new File(jbossTarget).renameTo(escalanteTarget)
  if (!renamed)
    error("Unable to rename %s to %s".format(jbossTarget, escalanteTarget))
}

// 2. Build Escalante, reusing the code used to unit test Escalante (how cool!!!)
println("Build modules and apply XML configuration changes")
AppServer.distSetUp(escalanteTarget, List(ArtifactModule, LiftModule))

// 4. Copy xsd files
println("Copy susystem XML schema files")
for (module <- List("lift", "artifact"))
yield
  copy("%s/../modules/%s/target/classes/schema".format(baseDir, module),
     "%s/docs/schema".format(escalanteTarget.getCanonicalPath))

println("Escalante assembled")

















