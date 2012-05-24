package org.scalabox.lift.assembly

import org.scalabox.util.FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import java.io.{File, FileOutputStream}
import org.jboss.as.controller.Extension
import org.scalabox.util.ScalaXmlParser._
import org.scalabox.maven.MavenArtifact
import org.scalabox.modules.JBossModulesRepository
import org.scalabox.lift.LiftExtension
import org.scalabox.assembly.ScalaBoxModule

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object LiftModule extends ScalaBoxModule {

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

      // 3. Install modules for Lift module dependencies
      val repo = new JBossModulesRepository(destDir)

      // TODO: There are duplicates and libraries we don't need:
//      -rw-r--r--   1 g  staff  489474 May 24 13:16 sisu-guice-3.0.3-no_aop.jar
//      -rw-r--r--   1 g  staff     221 May 24 13:16 sisu-guice-3.0.3-no_aop.jar.index
//      -rw-r--r--   1 g  staff  261829 May 24 13:16 sisu-inject-bean-2.2.3.jar
//      -rw-r--r--   1 g  staff     672 May 24 13:16 sisu-inject-bean-2.2.3.jar.index
//      -rw-r--r--   1 g  staff  203520 May 24 13:16 sisu-inject-plexus-2.2.3.jar
//      -rw-r--r--   1 g  staff    1440 May 24 13:16 sisu-inject-plexus-2.2.3.jar.index
//      -rw-r--r--   1 g  staff  223425 May 24 13:16 plexus-utils-2.0.6.jar
//      -rw-r--r--   1 g  staff     438 May 24 13:16 plexus-utils-2.0.6.jar.index
//      -rw-r--r--   1 g  staff  223943 May 24 13:16 plexus-utils-2.0.7.jar
//      -rw-r--r--   1 g  staff     438 May 24 13:16 plexus-utils-2.0.7.jar.index

      val subArtifacts =
         new MavenArtifact("org.apache.maven", "maven-settings", "3.0.4") ::
         new MavenArtifact("org.apache.maven", "maven-settings-builder", "3.0.4") ::
         new MavenArtifact("org.sonatype.aether", "aether-connector-wagon", "1.13.1") ::
         new MavenArtifact("org.apache.maven.wagon", "wagon-http-lightweight", "1.0") ::
         Nil

      repo.installModule(new MavenArtifact("org.apache.maven",
         "maven-aether-provider", "3.0.4"), subArtifacts)

      // 4. Generate module.xml and safe it to disk
      saveXml(new File(moduleDir, "module.xml"), moduleXml)

      // 4. Copy over the module jar
      copy(jarInput, new FileOutputStream(new File(moduleDir, jarName)))
   }

   private def moduleXml = {
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
