package org.scalabox.assembly

import java.io.File
import org.scalabox.util.FileSystem._
import org.scalabox.util.ScalaXmlParser._

/**
 * A JBoss Module repository
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class JBossModulesRepository(root: File) {

   def installModule(artifact: MavenArtifact, export: Boolean,
           deps: JBossModule*): JBossModule =
      doInstallModule(artifact, export, deps)

   def installModule(artifact: MavenArtifact, deps: JBossModule*): JBossModule =
      doInstallModule(artifact, false, deps)

   private def doInstallModule(artifact: MavenArtifact, export: Boolean,
           deps: Seq[JBossModule]): JBossModule = {
      // TODO: Check if already installed...
      val resolver = DependencyResolverFactory.getDependencyResolver()
      val jarFiles = resolver.artifact(artifact.coordinates).resolveAsFiles()

      val module = artifact.jbossModule(export)
      val dir = mkDirs(root, artifact.moduleDirName)

      val templateModuleXml =
         addXmlAttributes(
            <module xmlns="urn:jboss:module:1.0">
                  <resources />
                  <dependencies/>
            </module>, ("name", module.name), ("slot", module.slot))

      val resourceRoots = jarFiles.map { jar =>
         val name = jar.getName
         copy(jar, new File(dir, name))
         addXmlAttribute(<resource-root/>, "path", name)
      }

      val moduleXmlWithResources =
         addXmlElements("resources", resourceRoots, templateModuleXml)

      val moduleXml = deps match {
         case d => {
            val children = d.map {
               dep =>
                  addXmlAttributes(<module/>,
                     ("name", dep.name), ("export", dep.export.toString),
                     ("slot", dep.slot.toString),
                     ("services", dep.service.name))
            }
            addXmlElements("dependencies", children, moduleXmlWithResources)
         }
      }

      saveXml(new File(dir, "module.xml"), moduleXml)
      module
   }

}
