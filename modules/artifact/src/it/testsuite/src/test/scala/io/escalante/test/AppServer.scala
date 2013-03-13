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
import io.escalante.util.matching.RegularExpressions

/**
 * Set up and tear down helper methods for the application server
 * so that it's ready for testing deployments.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object AppServer extends Log with AssertionsForJUnit {

  final val Version = "7.x.incremental.667" // TODO: Avoid duplication with root pom

  final val TmpDir = System.getProperty("java.io.tmpdir")

  final val TestHome = new File(TmpDir, "jboss-as")

  def testUnzipAppServer() {
    unzipAppServer(TestHome, Version)
  }

  def unzipAppServer(home: File, version: String) {
    val xml = standaloneXmlPath(home)

    if (xml.exists())
      info("Base JBoss AS distribution already extracted")
    else {
      info(s"Unzip base JBoss AS distribution to ${home.getCanonicalPath}")

      if (home.exists()) {
        info("Old JBoss AS distribution exists, clean it up first")
        deleteDirectory(home)
      }

      val userHome = System.getProperty("user.home")
      val m2Repo = s"$userHome/.m2/repository"
      val jbossZip =
        s"$m2Repo/org/jboss/as/jboss-as-dist/$Version/jboss-as-dist-$Version.zip"

      // Unzip
      unzip(new File(jbossZip), new File(TmpDir))
      val unzippedDir = s"$TmpDir/jboss-as-$Version"
      val renamed = new File(unzippedDir).renameTo(home)
      if (!renamed)
        error(s"Unable to rename to $home")

      // Change permissions of .sh files
      findAll(new File(s"$home/bin"), RegularExpressions.ExecutableFilesRegex)
          .foreach(_.setExecutable(true))
    }
  }

  def testSetUp(modules: List[BuildableModule]) {
    val home = TestHome
    val modulesDir = new File(TmpDir, "test-module")

    // Cleanup thirdparty deployments module dir, if present
    deleteDirectoryIfPresent(new File(home, "thirdparty-modules"))

    // Delete directory is present...
    mkDirs(modulesDir, deleteIfPresent = true)

    // Set up modules
    setUp(home, modulesDir, modules, isTest = true)
  }

  def distSetUp(home: File, modules: List[BuildableModule]) {
    setUp(home, new File(s"$home/modules"), modules, isTest = false)
  }

  def tearDown() {
    val stdCfg = standaloneXmlPath(TestHome)
    val stdCfgOriginal = new File(s"${stdCfg.getCanonicalPath}.original")
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
    s"$home/standalone/configuration/standalone.xml")

  private def standaloneXmlBackupFile(cfg: File): File =
    new File(s"${cfg.getCanonicalPath}.original")

}
