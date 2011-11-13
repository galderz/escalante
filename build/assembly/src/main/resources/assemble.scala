// Assemble ScalaBox

import java.io._
import java.lang.String
import java.util.jar._
import collection.JavaConversions._
import xml._
import transform.{RuleTransformer, RewriteRule}

def use[T <: { def close(): Unit }](closable: T)(block: T => Unit) {
   try {
      block(closable)
   }
   finally {
      closable.close()
   }
}

def copy(in: InputStream, out: OutputStream) {
   use(in) { in =>
      use(out) { out =>
         val buffer = new Array[Byte](1024)
         Iterator.continually(in.read(buffer))
            .takeWhile(_ != -1)
            .foreach { out.write(buffer, 0 , _) }
      }
   }
}

def unzip(file: File, target: File) {
   val zip = new JarFile(file)
   enumerationAsScalaIterator(zip.entries).foreach { entry =>
       val entryPath = entry.getName
       println("Extracting to " + target.getCanonicalPath + "/" + entryPath)
       if (entry.isDirectory) {
          new File(target, entryPath).mkdirs
       } else {
          copy(zip.getInputStream(entry),
               new FileOutputStream(new File(target, entryPath)))
       }
   }
}

def copy(srcPath: String, destPath: String): Unit =
   copy(new File(srcPath), new File(destPath))

def copy(src: File,  dest: File) {
   if (src.isDirectory()) { // if directory not exists, create it
      if (!dest.exists()) {
         dest.mkdir();
         println("Directory copied from %s to %s"
               .format(src.getCanonicalPath, dest.getCanonicalPath))
      }
      // List all the directory contents
      asScalaIterator(src.list().iterator).foreach { file =>
         // Recursive copy
         copy(new File(src, file), new File(dest, file))
      }
   } else {
      copy(new FileInputStream(src), new FileOutputStream(dest))
      println("File copied from %s to %s"
            .format(src.getCanonicalPath, dest.getCanonicalPath))
   }
}

// XML utils
def addChild(n: Node, newChild: Node) = n match {
   case Elem(prefix, label, attribs, scope, child @ _*) =>
      Elem(prefix, label, attribs, scope, child ++ newChild : _*)
   case _ => error("Can only add children to elements!")
}

class AddChildrenTo(label: String, newChild: Node) extends RewriteRule {
   override def transform(n: Node) = n match {
      case n @ Elem(_, `label`, _, _, _*) => addChild(n, newChild)
      case other => other
   }
}

val baseDir = project.getBasedir.getCanonicalPath
val jbossVersion = project("version.jboss.as")
val scalaVersion = project("version.scala")

val targetDir = "%s/target".format(baseDir)
val userHome = System.getProperty("user.home")
val m2Repo = "%s/.m2/repository".format(userHome)
val jbossZip = "%1$s/org/jboss/as/jboss-as-dist/%2$s/jboss-as-dist-%2$s.zip"
        .format(m2Repo, jbossVersion)
val jbossPrefix = "jboss-as-"

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
val jbossDirs = target.listFiles(new FilenameFilter {
   def accept(dir: File, name: String) = name.startsWith(jbossPrefix)
})
if (jbossDirs == null)
   sys.error("Base dir %s does not exist!".format(baseDir))
if (jbossDirs.length > 0) {
   println("JBoss AS distribution already extracted")
} else {
   println("Unzip JBoss AS distribution to %s".format(target.getCanonicalPath))
   unzip(new File(jbossZip), target)
}
val jbossTarget = "%s/%s%s".format(targetDir, jbossPrefix, jbossVersion)
// Change permissions of .sh files
val executables = new File("%s/bin".format(jbossTarget)).listFiles(
   new FilenameFilter {
      def accept(dir: File, name: String) = name.endsWith(".sh")
   })
executables.foreach(_.setExecutable(true))

// 2. Copy over ScalaBox modules
val root = "%s/../..".format(baseDir)
copy("%s/modules/lift/target/module".format(root),
     "%s/modules".format(jbossTarget))
println("ScalaBox modules copied to JBoss AS")

// 3. Copy over third party modules
copy("%s/classes/modules".format(targetDir), "%s/modules".format(jbossTarget))
val scalaPath = "org/scala-lang/scala-library"
copy("%1$s/%2$s/%3$s/scala-library-%3$s.jar"
           .format(m2Repo, scalaPath, scalaVersion),
     "%s/modules/%s/main/scala-library.jar"
           .format(jbossTarget, scalaPath))
println("ScalaBox third-party modules copied to JBoss AS")

// 4. Add extension(s) to configuration file
val standaloneCfg = XML.loadFile(
   "%s/standalone/configuration/standalone.xml".format(jbossTarget))
val withExtension = new RuleTransformer(
   new AddChildrenTo("extensions", <extension module="org.scalabox.lift"/>))
        .transform(standaloneCfg).head
val withSubsystem = new RuleTransformer(
   new AddChildrenTo("profile",
            <subsystem xmlns="urn:org.scalabox:lift:1.0">
               <deployment-types>
                  <deployment-type suffix="sar" tick="10000"/>
                  <deployment-type suffix="war" tick="10000"/>
               </deployment-types>
            </subsystem>))
      .transform(withExtension).head
XML.save("%s/standalone/configuration/standalone.xml".format(jbossTarget),
         withSubsystem, "UTF-8", true, null)
println("Scalabox Lift extension added to configuration file")

















