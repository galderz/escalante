// Assemble ScalaBox

import java.io._
import java.lang.String
import org.scalabox.assembly.ScalaBox
import org.scalabox.lift.LiftModule
import org.scalabox.util.FileSystem._
import org.scalabox.util.ScalaXmlParser._

val baseDir = project.getBasedir.getCanonicalPath
val jbossVersion = project("version.jboss.as")
val scalaVersion = project("version.scala")
val projectVersion = project("version")
val jbossPrefix = "jboss-as-"
val scalaBoxPrefix = "scalabox-"
val userHome = System.getProperty("user.home")

val targetDir = "%s/target".format(baseDir)
val m2Repo = "%s/.m2/repository".format(userHome)
val jbossZip = "%1$s/org/jboss/as/jboss-as-dist/%2$s/jboss-as-dist-%2$s.zip"
        .format(m2Repo, jbossVersion)
val jbossTarget = "%s/%s%s".format(targetDir, jbossPrefix, jbossVersion)

// 0. Log startup
println()
println("""|------------------
           | Assemble ScalaBox
           |------------------
           | baseDir = %s
           | jbossVersion = %s
           | scalaVersion = %s
           """.format(baseDir, jbossVersion, scalaVersion).stripMargin)

// 1. Extract JBoss AS distro, if necessary...
val target = new File(targetDir)
val scalaBoxTarget = new File(
   "%s/%s".format(targetDir, scalaBoxPrefix + projectVersion))
val scalaBoxDirs = target.listFiles(new FilenameFilter {
   def accept(dir: File, name: String) = name.startsWith(scalaBoxPrefix)
})
if (scalaBoxDirs == null) error("Base dir %s does not exist!".format(baseDir))
if (scalaBoxDirs.length > 0) {
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
   // 2. Rename to ScalaBox with version
   val renamed = new File(jbossTarget).renameTo(scalaBoxTarget)
   if (!renamed)
      error("Unable to rename %s to %s".format(jbossTarget, scalaBoxTarget))
}

// 2. Build Scalabox, reusing the code used to unit test ScalaBox (how cool!!!)
ScalaBox.build(new File("%s/modules".format(scalaBoxTarget)), LiftModule)

// 3. Add extension(s) and subsystem(s) to configuration file
val stdCfg = new File(
   "%s/standalone/configuration/standalone.xml".format(scalaBoxTarget))
val stdCfgOriginal = new File("%s.original".format(stdCfg.getCanonicalPath))
if (!stdCfgOriginal.exists())
   copy(stdCfg, stdCfgOriginal) // Backup original standalone config

val withExtension = addXmlElement(
   "extensions", <extension module="org.scalabox.lift"/>, stdCfgOriginal)
val withSubsystem = addXmlElement(
   "profile", <subsystem xmlns="urn:scalabox:lift:1.0" />, withExtension)

saveXml(stdCfg.getCanonicalPath, withSubsystem)
println("Scalabox Lift extension added to configuration file")

















