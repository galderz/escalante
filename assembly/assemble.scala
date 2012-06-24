// Assemble Escalante

import java.io._
import java.lang.String
import io.escalante.assembly.RuntimeAssembly
import io.escalante.lift.assembly.LiftModule
import io.escalante.util.FileSystem._
import io.escalante.util.ScalaXmlParser._

val baseDir = project.getBasedir.getCanonicalPath
val jbossVersion = project("version.jboss.as")
val scalaVersion = project("version.scala")
val projectVersion = project("version")
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
println("""|-------------------
           | Assemble Escalante
           |-------------------
           | baseDir = %s
           | jbossVersion = %s
           | scalaVersion = %s
           """.format(baseDir, jbossVersion, scalaVersion).stripMargin)

// 1. Extract JBoss AS distro, if necessary...
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
RuntimeAssembly.build(new File("%s/modules".format(escalanteTarget)), LiftModule)

// 3. Add extension(s) and subsystem(s) to configuration file
val stdCfg = new File(
   "%s/standalone/configuration/standalone.xml".format(escalanteTarget))
val stdCfgOriginal = new File("%s.original".format(stdCfg.getCanonicalPath))
if (!stdCfgOriginal.exists())
   copy(stdCfg, stdCfgOriginal) // Backup original standalone config

val withExtension = addXmlElement(
   "extensions", <extension module="io.escalante.lift"/>, stdCfgOriginal)
val withSubsystem = addXmlElement(
   "profile",
      <subsystem xmlns="urn:escalante:lift:1.0">
         <thirdparty-modules-repo relative-to="jboss.home.dir" path="modules" />
      </subsystem>,
   withExtension)

saveXml(stdCfg, withSubsystem)
println("Escalante Lift extension added to configuration file")

//// 4. Modify standalone.sh (and other scripts...) to add downloads module dir
//val standaloneSh = new File("%s/bin/standalone.sh".format(escalanteTarget))
//val standaloneShOriginal = new File(
//      "%s.original".format(standaloneSh.getCanonicalPath))
//if (!standaloneShOriginal.exists())
//   copy(standaloneSh, standaloneShOriginal) // Backup original standalone config
//
//val standaloneShContents = fileToString(standaloneSh, "UTF-8")
//val newStandaloneShContents = standaloneShContents.replace("$JBOSS_HOME/modules",
//      "$JBOSS_HOME/modules:$JBOSS_HOME/thirdparty-modules")
//printToFile(standaloneSh) { p =>
//   p.print(newStandaloneShContents)
//}
















