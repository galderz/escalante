package org.scalabox.lift

import assembly.LiftModule
import org.scalabox.util.FileSystem._
import org.scalabox.util.Closeable._
import org.jboss.as.controller.client.ModelControllerClient
import java.net.InetAddress
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalabox.logging.Log
import org.jboss.as.controller.operations.common.Util
import org.jboss.as.controller.{ControlledProcessState, PathAddress}
import org.scalatest.junit.AssertionsForJUnit
import java.io.File
import org.scalabox.assembly.RuntimeAssembly

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
abstract class LiftTestSetup {
   // To avoid being instantiated by the surefire...
}

object LiftTestSetup extends AssertionsForJUnit with Log {

   def buildExtension {
      info("Build Lift extension and copy to container")
      // Cleanup scala deployments module dir, if present
      deleteDirectoryIfPresent(
         new File(System.getProperty("surefire.basedir", ".")
              + "/build/target/jboss-as/downloads"))
      // Set up Lift module
      val tmpFile = new File(System.getProperty("java.io.tmpdir"))
      val destDir = mkDirs(tmpFile, "test-module", true) // Delete if present!
      info("Build Lift module into %s", destDir)
      RuntimeAssembly.build(destDir, LiftModule)
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
            // TODO: Once https://issues.jboss.org/browse/AS7-4185 is fixed, remove this
            Thread.sleep(3000)

            // Now that the reload has been requested, do a dummy operation to
            // wait for the server to be up and running
            waitServerToStart(client)
      }
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
         null
      } else {
         assert(SUCCESS === outcome)
         r.get(RESULT)
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

   private def isServerRunning(rsp: ModelNode): Boolean = {
      ControlledProcessState.State.RUNNING.toString == rsp.get(RESULT).asString()
   }

   private def createReadServerStateOp: ModelNode = {
      val op = Util.getEmptyOperation(READ_ATTRIBUTE_OPERATION, PathAddress.EMPTY_ADDRESS.toModelNode())
      op.get(NAME).set("server-state")
      op
   }

}