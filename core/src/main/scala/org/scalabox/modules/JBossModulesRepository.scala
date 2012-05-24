package org.scalabox.modules

import java.io.File
import org.scalabox.util.FileSystem._
import org.scalabox.util.ScalaXmlParser._
import org.scalabox.maven.{MavenArtifact, MavenDependencyResolver}
import org.scalabox.Scala
import xml.{Node, Elem}

/**
 * A JBoss Module repository
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class JBossModulesRepository(root: File) {

   /**
    * Installs a Scala library version as a JBoss Module.
    */
   def installScalaModule(scala: Scala): JBossModule =
      installModule(scala.maven, scala.moduleXml)

   /**
    * Installs a JBoss module from a Maven artifact and a set of dependencies
    * for this module. During the installation process, the module.xml file
    * will be generated, hence, this method is recommended for those modules
    * that have complex/big module descriptors such as those that contain a
    * lot of jars (possibly coming from transitive dependencies).
    */
   def installModule(artifact: MavenArtifact, deps: JBossModule*): JBossModule =
      installModule(artifact, export = false, deps = deps,
         moduleDescriptor = None, subArtifacts = Nil)

   /**
    * Install a JBoss module from a Maven artifact with a given module.xml.
    * This method is recommended for modules with relatively simple module
    * descriptors, such as single jar modules, or small module descriptors
    * with exotic options.
    */
   def installModule(artifact: MavenArtifact, moduleXml: Elem): JBossModule =
      installModule(artifact, export = false, deps = Nil,
         moduleDescriptor = Some(moduleXml), subArtifacts = Nil)

   /**
    * Install a JBoss module from a Maven artifact alongside other Maven
    * sub-artifacts. These sub-artifacts will be resolved and installed under
    * the same JBoss module as the main artifact.
    */
   def installModule(artifact: MavenArtifact,
           subArtifacts: List[MavenArtifact]): JBossModule =
      installModule(artifact, export = false, deps = Nil,
         moduleDescriptor = None, subArtifacts = subArtifacts)

   /**
    *
    * @param artifact
    * @param export
    * @param deps
    * @param moduleDescriptor
    * @return
    */
   private def installModule(artifact: MavenArtifact, export: Boolean,
           deps: Seq[JBossModule], moduleDescriptor: Option[Node],
           subArtifacts: List[MavenArtifact]): JBossModule = {
      // TODO: check if the directory exists, a module.xml exists and at least one jar is present

      val module = artifact.jbossModule(export)
      val dir = mkDirs(root, artifact.moduleDirName)

      // Take all artifacts, both main artifact and sub-artifact, and create a single list will all the jar files
      val jarFiles = (artifact :: subArtifacts)
              .flatMap(MavenDependencyResolver.resolveArtifact(_))

      jarFiles.foreach(jar => copy(jar, new File(dir, jar.getName)))

      val moduleXml = moduleDescriptor.getOrElse {
         val templateModuleXml =
            addXmlAttributes(
               <module xmlns="urn:jboss:module:1.0">
                     <resources/>
                     <dependencies/>
               </module>, ("name", module.name), ("slot", module.slot))

         val resourceRoots = jarFiles.map {
            jar => addXmlAttribute(<resource-root/>, "path", jar.getName)
         }

         val moduleXmlWithResources =
            addXmlElements("resources", resourceRoots, templateModuleXml)

         deps match {
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
      }

      saveXml(new File(dir, "module.xml"), moduleXml)
      module
   }

}