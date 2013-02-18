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
import java.io.File
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

  def testSetUp(modules: List[BuildableModule]) {
    val home = testHome()
    val modulesDir =
      new File(System.getProperty("java.io.tmpdir"), "test-module")

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

  private def testHome(): File =
    new File(System.getProperty("surefire.basedir", ".")
        + "/build/target/jboss-as")

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

    // TODO: Change logging settings when testing to see debug/trace info
    // Includes: adding:
    // <logger category="io.escalante"><level name="TRACE"/></logger>
    // And change CONSOLE level to TRACE

    // Save the XML
    saveXml(xml, config)
  }

  def tearDown() {
    val stdCfg = standaloneXmlPath(testHome())
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

  def backupStandaloneXml(home: File): (File, File) = {
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
