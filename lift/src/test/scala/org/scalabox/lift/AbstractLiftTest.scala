package org.scalabox.lift

import org.scalabox.util.FileSystem._
import org.jboss.shrinkwrap.api.ShrinkWrap
import org.jboss.shrinkwrap.api.spec.JavaArchive
import org.jboss.shrinkwrap.api.exporter.ZipExporter
import java.io.{File, FileOutputStream}
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver
import org.scalabox.util.Closeable._
import org.jboss.as.controller.client.ModelControllerClient
import java.net.InetAddress
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalabox.logging.Log
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset
import org.scalatest.junit.AssertionsForJUnit
import org.jboss.as.controller.operations.common.Util
import org.jboss.as.controller.{ControlledProcessState, PathAddress, Extension}
import xml.transform.{RewriteRule, RuleTransformer}
import xml._


/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class AbstractLiftTest extends AssertionsForJUnit {

}

object AbstractLiftTest extends AssertionsForJUnit with Log {

   val SCALA_VERSION = "2.9.1"
   val LIFT_VERSION = "2.4"
   val INPATH_SCALA_VERSION = SCALA_VERSION.replace('.', '_')

   def buildExtension {
      info("Build Lift extension and copy to container")

      // Set up test module path
      val destDir = mkDirs(getTarget, "test-module", true)
      val moduleDir = mkDirs(destDir, "org/scalabox/lift/main")

      // Create jar with the extension
      val archive = ShrinkWrap.create(classOf[JavaArchive], "scalabox-lift.jar")
      archive.addPackages(true, "org/scalabox")
      archive.addAsServiceProvider(classOf[Extension], classOf[LiftExtension])
      val jarInput = archive.as(classOf[ZipExporter]).exportAsInputStream()

      // Copy over the module descriptor
      copy(new ClassLoaderAsset("module/main/module.xml").openStream(),
         new FileOutputStream(new File(moduleDir, "module.xml")))

      // Copy over the module jar
      copy(jarInput, new FileOutputStream(
         new File(destDir, "org/scalabox/lift/main/scalabox-lift.jar")))

      // Copy module dependencies for Scala
      copyModuleDeps("org.scala-lang", "scala-library", SCALA_VERSION,
         "scala-library.jar", destDir)
      // Copy module dependencies for Lift
      copyLiftDeps(destDir)
   }

   private def copyLiftDeps(destDir: File) {
      // Copy apache dependency first
      copyModuleDeps("commons-fileupload", "commons-fileupload", "1.2.2",
         "commons-fileupload-1.2.2.jar", destDir,
         Some(List(new Module("javax.servlet.api"))))

      val moduleDeps = Map(
         "common" -> List(new Module("org.slf4j")),
         "json" -> List(),
         "actor" -> List(
            new Module("net.liftweb.lift-common_%s".format(INPATH_SCALA_VERSION), true)),
         "util" -> List(
            new Module("net.liftweb.lift-actor_%s".format(INPATH_SCALA_VERSION), true),
            new Module("org.joda.time")),
         "webkit" -> List(new Module("javax.servlet.api"),
            new Module("commons-fileupload.commons-fileupload"),
            new Module("net.liftweb.lift-json_%s".format(INPATH_SCALA_VERSION), true),
            new Module("net.liftweb.lift-util_%s".format(INPATH_SCALA_VERSION), true),
            // TODO: Is this sane?
            new Module("org.scalabox.lift"))
      )
      moduleDeps.foreach {
         case (module, deps) =>
            copyModuleDeps("net.liftweb", "lift-%s_%s".format(module, SCALA_VERSION), LIFT_VERSION,
               "lift-%s_%s-%s.jar".format(module, SCALA_VERSION, LIFT_VERSION), destDir, Some(deps))
      }
   }

   private def copyModuleDeps(groupId: String, artifactId: String,
                              version: String, jarName: String, destDir: File, deps: Option[List[Module]]) {
      val jar = DependencyResolvers.use(classOf[MavenDependencyResolver])
            .artifact("%s:%s:%s".format(groupId, artifactId, version))
            .resolveAsFiles()(0)
      val moduleDir = groupId.replace('.', '/')
      // Replace any dots in the artifact id by underscores
      val validArtifactId = artifactId.replace('.', '_')
      val dir = mkDirs(destDir,
         "%s/%s/main".format(moduleDir, validArtifactId))
      copy(jar, new File(dir, jarName))
      val templateModuleXml =
         <module xmlns="urn:jboss:module:1.0">
            <resources>
               {<resource-root/> % Attribute(None, "path", Text(jarName), Null)}
            </resources>
               <dependencies/>
         </module> % Attribute(None, "name", Text("%s.%s".format(groupId, validArtifactId)), Null)

      val moduleXml = deps match {
         case Some(d) => {
            val allDeps = new Module("org.scala-lang.scala-library", false) :: d
            val children = allDeps.map {
               dep =>
                  new AddChildrenTo("dependencies", <module/> %
                        Attribute(None, "name", Text(dep.name), Null) %
                        Attribute(None, "export", Text(dep.export.toString), Null)
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
                              version: String, jarName: String, destDir: File) {
      copyModuleDeps(groupId, artifactId, version, jarName, destDir, None)
   }

   def installExtension {
      // Add the extension and subsystem
      info("Add Lift extension to container")
      use(ModelControllerClient.Factory.create(
         InetAddress.getByName("localhost"), 9999)) {
         client =>
            val opAddExt = new ModelNode()
            opAddExt.get(OP).set(ADD)
            opAddExt.get(OP_ADDR).add("extension", "org.scalabox.lift")

            val opAddSubsystem = new ModelNode()
            opAddSubsystem.get(OP).set(ADD)
            opAddSubsystem.get(OP_ADDR).add("subsystem", "lift")

            validateResponse(client.execute(opAddExt))
            validateResponse(client.execute(opAddSubsystem))

            info("Lift extension and subsystem added, now reload the server")

            // Reload the app server so that it's rebooted and deployment
            // unit processors (DUPs) are installed (only happens at boot)
            val opReload = new ModelNode()
            opReload.get(OP).set("reload")
            opAddSubsystem.get(OP_ADDR).setEmptyList()
            validateResponse(client.execute(opReload))

            // Sleep for a little bit to allow enough time for server to start
            // TODO: Find a better way of waiting till server's reloaded
            Thread.sleep(3000)

            // Now that the reload has been requested, do a dummy operation to
            // wait for the server to be up and running
            waitServerToStart(client)
      }
   }

   private def waitServerToStart(client: ModelControllerClient) {
      info("Wait for server reload...")
      waitServerToStart(createReadServerStateOp, client)
      info("Wait over, server reloaded")
   }

   private def waitServerToStart(op: ModelNode, client: ModelControllerClient) {
      val rsp = client.execute(op)
      validateResponse(rsp)
      if (!isServerRunning(rsp)) {
         Thread.sleep(50)
         waitServerToStart(createReadServerStateOp, client)
      }
   }

   def isServerRunning(rsp: ModelNode): Boolean = {
      ControlledProcessState.State.RUNNING.toString == rsp.get(RESULT).asString()
   }

   private def createReadServerStateOp: ModelNode = {
      val op = Util.getEmptyOperation(READ_ATTRIBUTE_OPERATION, PathAddress.EMPTY_ADDRESS.toModelNode())
      op.get(NAME).set("server-state")
      op
   }


   def uninstallExtension {
      // Remove the extension and subsystem
      info("Uninstall Lift extension from container")
      use(ModelControllerClient.Factory.create(
         InetAddress.getByName("localhost"), 9999)) {
         client =>
            val opRemoveExt = new ModelNode()
            opRemoveExt.get(OP).set("remove")
            opRemoveExt.get(OP_ADDR).add("extension", "org.scalabox.lift")

            val opRemoveSubsystem = new ModelNode()
            opRemoveSubsystem.get(OP).set("remove")
            opRemoveSubsystem.get(OP_ADDR).add("subsystem", "lift")

            validateResponse(client.execute(opRemoveSubsystem))
            validateResponse(client.execute(opRemoveExt))
      }
   }

   def validateResponse(r: ModelNode): ModelNode = {
      val outcome = r.get(OUTCOME).asString()
      if (outcome == FAILED) {
         val failure = r.get(FAILURE_DESCRIPTION).asString
         if (failure.contains("Duplicate resource")) {
            // If duplicate resource found, it could be due to test not
            // having finished properly, so clean up before propagating failure
            info("Duplicate extension found, carry on...")
            uninstallExtension
         }
         fail(r.toString)
      } else {
         assert(SUCCESS === outcome, r)
         r.get(RESULT)
      }
   }

   private class Module(val name: String, val export: Boolean) {

      def this(name: String) = this(name, false)

   }

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