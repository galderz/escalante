package org.scalabox.assembly

import org.scalabox.util.FileSystem._
import java.io.File
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.scalabox.logging.Log
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml._

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object ScalaBox extends Log {

   val SCALA_29 = "2.9.1"
   val SCALA_28 = "2.8.2"
   val LIFT_24 = "2.4"
   val INPATH_SCALA_VERSION = SCALA_29.replace('.', '_')

   def build(destDir: File, module: JBossModule) {
      info("Build Lift extension and copy to container")

      module.build(destDir)

      // Copy module dependencies for Scala
      copyModuleDeps("org.scala-lang", "scala-library", SCALA_29,
                     "scala-library.jar", destDir, "main")
      copyModuleDeps("org.scala-lang", "scala-library", SCALA_28,
                     "scala-library.jar", destDir, SCALA_28)

      // TODO: Move below to LiftModule
      // Copy module dependencies for Lift
      copyLiftDeps(destDir, SCALA_29, "main")
      // Copy module dependencies for Lift
      copyLiftDeps(destDir, SCALA_28, SCALA_28)
   }

   private def pathFriendly(version: String) = version.replace('.', '_')

   private def copyModuleDeps(groupId: String, artifactId: String,
            version: String, jarName: String, destDir: File,
            deps: Option[List[Module]], slot: String) {
      val jar = DependencyResolvers.use(classOf[MavenDependencyResolver])
            .artifact("%s:%s:%s".format(groupId, artifactId, version))
            .resolveAsFiles()(0)
      val moduleDir = groupId.replace('.', '/')
      // Replace any dots in the artifact id by underscores
      val validArtifactId = artifactId.replace('.', '_')
      val dir = mkDirs(destDir,
                       "%s/%s/%s".format(moduleDir, validArtifactId, slot))
      copy(jar, new File(dir, jarName))
      val templateModuleXml =
         <module xmlns="urn:jboss:module:1.0">
            <resources>
               {<resource-root/> % Attribute(None, "path", Text(jarName), Null)}
            </resources>
               <dependencies/>
         </module> %
               Attribute(None, "name", Text("%s.%s".format(groupId, validArtifactId)), Null) %
               Attribute(None, "slot", Text(slot), Null)

      val moduleXml = deps match {
         case Some(d) => {
            val children = d.map {
               dep =>
                  new AddChildrenTo("dependencies", <module/> %
                        Attribute(None, "name", Text(dep.name), Null) %
                        Attribute(None, "export", Text(dep.export.toString), Null) %
                        Attribute(None, "slot", Text(dep.slot.toString), Null)
                  )
            }
            new RuleTransformer(children: _*).transform(templateModuleXml).head
         }
         case None => templateModuleXml
      }

      XML.save(new File(dir, "module.xml").getCanonicalPath,
               moduleXml, "UTF-8", true, null)
   }

   private def copyModuleDeps(groupId: String, artifactId: String,
      version: String, jarName: String, destDir: File, slot: String) {
      copyModuleDeps(groupId, artifactId, version, jarName, destDir, None, slot)
   }

   private def copyLiftDeps(destDir: File, scalaVersion: String, scalaSlot: String) {
      // Copy apache dependency first
      copyModuleDeps("commons-fileupload", "commons-fileupload", "1.2.2",
                     "commons-fileupload-1.2.2.jar", destDir,
                     Some(List(new Module("javax.servlet.api"))), "main")

      val moduleDeps = Map(
         "common" -> List(new Module("org.slf4j"),
                          new Module("org.scala-lang.scala-library", true, scalaSlot)),
         "json" -> List(),
         "actor" -> List(
            new Module("net.liftweb.lift-common_%s".format(
               pathFriendly(scalaVersion)), true)),
         "util" -> List(
            new Module("net.liftweb.lift-actor_%s".format(
               pathFriendly(scalaVersion)), true),
            new Module("org.joda.time")),
         "webkit" -> List(new Module("javax.servlet.api"),
                          new Module("commons-fileupload.commons-fileupload"),
                          new Module("net.liftweb.lift-json_%s".format(
                             pathFriendly(scalaVersion)), true),
                          new Module("net.liftweb.lift-util_%s".format(
                             pathFriendly(scalaVersion)), true),
                          // TODO: Is this sane?
                          new Module("org.scalabox.lift"))
      )
      moduleDeps.foreach {
         case (module, deps) =>
            copyModuleDeps("net.liftweb", "lift-%s_%s".format(module, scalaVersion), LIFT_24,
               "lift-%s_%s-%s.jar".format(module, scalaVersion, LIFT_24), destDir, Some(deps), "main")
      }
   }

   private class Module(val name: String, val export: Boolean, val slot: String) {

      def this(name: String) = this(name, false, "main")

      def this(name: String, export: Boolean) = this(name, export, "main")

   }

   // TODO: Refactor this class and other methods into ScalaXmlParser

   private class AddChildrenTo(label: String, newChild: Node) extends RewriteRule {

      override def transform(n: Node) = n match {
         case n@Elem(_, `label`, _, _, _*) => addChild(n, newChild)
         case other => other
      }

      def addChild(n: Node, newChild: Node) = n match {
         case Elem(prefix, label, attribs, scope, child@_*) =>
            Elem(prefix, label, attribs, scope, child ++ newChild: _*)
         case _ => error("Can only add children to elements!")
      }

   }
}
