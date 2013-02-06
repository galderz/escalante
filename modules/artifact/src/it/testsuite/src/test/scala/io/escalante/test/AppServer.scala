/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test

import io.escalante.logging.Log
import io.escalante.io.FileSystem._
import io.escalante.io.Closeable._
import java.io.{FileOutputStream, File}
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalatest.junit.AssertionsForJUnit
import org.jboss.as.controller.client.ModelControllerClient
import java.net.InetAddress

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object AppServer extends Log with AssertionsForJUnit {

  private val jbossHome = new File(
    System.getProperty("surefire.basedir", ".") + "/build/target/jboss-as")

  private val testModuleDir =
    new File(System.getProperty("java.io.tmpdir"), "test-module")

  def setUp(standaloneXml: String): File = {
    // Cleanup thirdparty deployments module dir, if present
    deleteDirectoryIfPresent(new File(jbossHome, "thirdparty-modules"))

    // Delete directory is present...
    mkDirs(testModuleDir, deleteIfPresent = true)
    info("Build module into %s", testModuleDir)

    // Backup standalone configuration
    backupStandaloneXml(jbossHome)

    // Copy test standalone xml to server
    copy(new ClassLoaderAsset(standaloneXml).openStream(),
      new FileOutputStream(standaloneXmlPath(jbossHome)))

    testModuleDir
  }

  def tearDown() {
    val stdCfg = standaloneXmlPath(jbossHome)
    val stdCfgOriginal = new File("%s.original".format(stdCfg.getCanonicalPath))
    copy(stdCfgOriginal, stdCfg) // Restore original standalone config
  }

  def assertExtensionInstalled(extensionName: String) {
    use(ModelControllerClient.Factory.create(
      InetAddress.getByName("localhost"), 9999)) {
      client =>
        val op = new ModelNode()
        op.get(OP).set(READ_RESOURCE_DESCRIPTION_OPERATION)
        op.get(OP_ADDR).add("extension", extensionName)
        val resp = client.execute(op)
        validateResponse(resp)
        info(extensionName + " is installed: %s", resp.get(OUTCOME))
    }
  }

  private def validateResponse(r: ModelNode): ModelNode = {
    val outcome = r.get(OUTCOME).asString()
    if (outcome == FAILED) {
      val failure = r.get(FAILURE_DESCRIPTION).asString
      if (failure.contains("Duplicate resource")) {
        // If duplicate resource found, it could be due to test not
        // having finished properly, so clean up before propagating failure
        info("Duplicate extension found, carry on...")
        tearDown()
      }
      fail(r.toString)
      null
    } else {
      assert(SUCCESS === outcome)
      r.get(RESULT)
    }
  }

  private def standaloneXmlPath(home: File) = new File(
    "%s/standalone/configuration/standalone.xml".format(home))

  private def backupStandaloneXml(home: File): (File, File) = {
    val cfg = standaloneXmlPath(home)
    val cfgBackup = standaloneXmlBackupFile(cfg)
    if (!cfgBackup.exists())
      copy(cfg, cfgBackup) // Backup original standalone config

    (cfg, cfgBackup)
  }

  private def standaloneXmlBackupFile(cfg: File): File =
    new File("%s.original".format(cfg.getCanonicalPath))

}
