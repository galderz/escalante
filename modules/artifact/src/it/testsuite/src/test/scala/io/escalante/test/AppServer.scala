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
import java.io.{FilenameFilter, File}
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.scalatest.junit.AssertionsForJUnit
import org.jboss.as.controller.client.ModelControllerClient
import java.net.InetAddress
import scala.xml.{XML, Node}
import io.escalante.xml.ScalaXmlParser._
import annotation.tailrec

/**
 * Set up and tear down helper methods for the application server
 * so that it's ready for testing deployments.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object AppServer extends Log with AssertionsForJUnit {

  val VERSION = "7.x.incremental.667" // TODO: Avoid duplication with root pom

  val TMP_DIR = System.getProperty("java.io.tmpdir")

  val TEST_HOME = new File(TMP_DIR, "jboss-as")

  def testUnzipAppServer() {
    unzipAppServer(TEST_HOME, VERSION)
  }

  def unzipAppServer(home: File, version: String) {
    val xml = standaloneXmlPath(home)

    if (xml.exists())
      info("Base JBoss AS distribution already extracted")
    else {
      info("Unzip base JBoss AS distribution to %s"
          .format(home.getCanonicalPath))

      val userHome = System.getProperty("user.home")
      val m2Repo = "%s/.m2/repository".format(userHome)
      val jbossZip = "%1$s/org/jboss/as/jboss-as-dist/%2$s/jboss-as-dist-%2$s.zip"
          .format(m2Repo, VERSION)

      // Unzip
      unzip(new File(jbossZip), new File(TMP_DIR))
      val unzippedDir = "%s/jboss-as-%s".format(TMP_DIR, VERSION)
      val renamed = new File(unzippedDir).renameTo(home)
      if (!renamed)
        error("Unable to rename to %s".format(home))

      // Change permissions of .sh files
      val executables = new File("%s/bin".format(home)).listFiles(
        new FilenameFilter {
          def accept(dir: File, name: String) = name.endsWith(".sh")
        })
      executables.foreach(_.setExecutable(true))
    }
  }

  def testSetUp(modules: List[BuildableModule]) {
    val home = TEST_HOME
    val modulesDir = new File(TMP_DIR, "test-module")

    // Cleanup thirdparty deployments module dir, if present
    deleteDirectoryIfPresent(new File(home, "thirdparty-modules"))

    // Delete directory is present...
    mkDirs(modulesDir, deleteIfPresent = true)

    // Set up modules
    setUp(home, modulesDir, modules, isTest = true)
  }

  def distSetUp(home: File, modules: List[BuildableModule]) {
    setUp(home, new File("%s/modules".format(home)), modules, isTest = false)
  }

  def tearDown() {
    val stdCfg = standaloneXmlPath(TEST_HOME)
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

  private def setUp(
      home: File,
      modulesDir: File,
      modules: List[BuildableModule],
      isTest: Boolean) {
    info("Build modules %s into %s", modules, modulesDir)

    // Backup standalone configuration
    val (xml, xmlBackup) = backupStandaloneXml(home)
    val xmlForEdit: Node = XML.loadFile(xmlBackup)

    // Build each module and let it apply changes to XML
    val config = buildModules(modulesDir, modules, xmlForEdit)

    // Save the XML
    saveXml(xml, config)
  }

  private def backupStandaloneXml(home: File): (File, File) = {
    val cfg = standaloneXmlPath(home)
    val cfgBackup = standaloneXmlBackupFile(cfg)
    if (!cfgBackup.exists())
      copy(cfg, cfgBackup) // Backup original standalone config

    (cfg, cfgBackup)
  }

  @tailrec
  private def buildModules(
      modulesDir: File,
      modules: List[BuildableModule],
      config: Node): Node = {
    modules match {
      case List() => config
      case module :: moreModules =>
        buildModules(modulesDir, moreModules, module.build(modulesDir, config))
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

  private def standaloneXmlBackupFile(cfg: File): File =
    new File("%s.original".format(cfg.getCanonicalPath))

}
