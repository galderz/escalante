package org.scalabox.lift

import org.scalabox.assembly.JBossModule
import org.scalabox.util.FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import java.io.{File, FileOutputStream}
import org.jboss.as.controller.Extension
import scala.xml._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftModule extends JBossModule {

   def build(destDir: File) {
      val modulePath = "org/scalabox/lift/main"
      val moduleDir = new File(destDir, modulePath)
      val jarName = "scalabox-lift.jar"

      // 1. Create directories
      mkDirs(destDir, modulePath)

      // 2. Create jar with the extension
      val archive = ShrinkWrap.create(classOf[JavaArchive], jarName)
      archive.addPackages(true, "org/scalabox")
      archive.addAsServiceProvider(classOf[Extension], classOf[LiftExtension])
      val jarInput = archive.as(classOf[ZipExporter]).exportAsInputStream()

      // 3. Generate module.xml and safe it to disk
      val moduleXml =
         <module xmlns="urn:jboss:module:1.0" name="org.scalabox.lift">
            <resources>
               <resource-root path="scalabox-lift.jar"/>
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
               <!-- TODO: Is this sane? -->
               <module name="net.liftweb.lift-webkit_2_9_1"/>
            </dependencies>
         </module>

      XML.save(new File(moduleDir, "module.xml").getCanonicalPath,
               moduleXml, "UTF-8", true, null)

      // 4. Copy over the module jar
      copy(jarInput, new FileOutputStream(new File(moduleDir, jarName)))
   }

}
