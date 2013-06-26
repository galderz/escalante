/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test

import annotation.tailrec
import io.escalante.io.Closeable._
import io.escalante.io.FileSystem._
import io.escalante.logging.Log
import io.escalante.util.matching.RegularExpressions
import io.escalante.xml.ScalaXmlParser._
import java.io.File
import java.net.InetAddress
import org.jboss.as.controller.client.ModelControllerClient
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import org.jboss.dmr.ModelNode
import org.scalatest.junit.AssertionsForJUnit
import scala.xml.{XML, Node}

/**
 * Set up and tear down helper methods for the application server
 * so that it's ready for testing deployments.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object AppServer extends Log with AssertionsForJUnit {

  private final val Version = "7.x.incremental.667" // TODO: Avoid duplication with root pom

  private final val TmpDir = System.getProperty("java.io.tmpdir")

  private final val TestHome = new File(TmpDir, "jboss-as")

  private final val TestHomeContainer0 = new File(TmpDir, "jboss-as-clustering-0")

  private final val TestHomeContainer1 = new File(TmpDir, "jboss-as-clustering-1")

  private final val TestModulesDir = new File(TmpDir, "test-module")

  private final val TestModulesDirContainer0 = new File(TmpDir, "test-module-clustering-0")

  private final val TestModulesDirContainer1 = new File(TmpDir, "test-module-clustering-1")

  private final val TestStandaloneXml = standaloneXmlPath(TestHome)

  private final val TestStandaloneHaXmlContainer0 = standaloneHaXmlPath(TestHomeContainer0)

  private final val TestStandaloneHaXmlContainer1 = standaloneHaXmlPath(TestHomeContainer1)

  def setUpAppServer() {
    unzipAppServer(TestHome, Version)
  }

  def setUpAppServerCluster() {
    setUpAppServer()
    // Make two copies of the unzipped server for clustering
    copy(TestHome, TestHomeContainer0)
    copy(TestHome, TestHomeContainer1)
  }

  def unzipAppServer(home: File, version: String) {
    if (standaloneXmlPath(home).exists())
      println("Base JBoss AS distribution already extracted")
    else {
      println(s"Unzip base JBoss AS distribution to ${home.getCanonicalPath}")

      if (home.exists()) {
        println("Old JBoss AS distribution exists, clean it up first")
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

  def setUpModules(modules: List[BuildableModule], configs: List[File] = List(TestStandaloneXml)) {
    val home = TestHome
    val modulesDir = new File(TmpDir, "test-module")

    // Cleanup thirdparty deployments module dir, if present
    deleteDirectoryIfPresent(new File(home, "thirdparty-modules"))

    // Delete directory is present...
    mkDirs(modulesDir, deleteIfPresent = true)

    // Set up modules
    setUp(home, modulesDir, modules, isTest = true, configs)
  }

  def setUpModulesCluster(modules: List[BuildableModule]) {
    setUpModules(modules, List(TestStandaloneHaXmlContainer0, TestStandaloneHaXmlContainer1))
    copy(TestModulesDir, TestModulesDirContainer0)
    copy(TestModulesDir, TestModulesDirContainer1)
  }

  def distSetUp(home: File, modules: List[BuildableModule]) {
    setUp(home, new File(s"$home/modules"), modules, isTest = false,
      List(standaloneXmlPath(home), standaloneHaXmlPath(home)))
  }

  def tearDownAppServer() {
    restoreStandaloneXml(TestStandaloneXml)
  }

  def tearDownAppServerCluster() {
    restoreStandaloneXml(TestStandaloneHaXmlContainer0)
    restoreStandaloneXml(TestStandaloneHaXmlContainer1)
  }

  private def restoreStandaloneXml(standaloneXml: File) {
    val stdCfgOriginal = new File(s"${standaloneXml.getCanonicalPath}.original")
    copy(stdCfgOriginal, standaloneXml) // Restore original standalone config
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
      isTest: Boolean,
      appServerConfigs: List[File]) {
    info("Build modules %s into %s and apply changes in %s", modules, modulesDir, appServerConfigs)

    appServerConfigs.foreach { cfg =>
    // Backup standalone configuration
      val (xml, xmlBackup) = backupStandaloneXml(cfg)
      val xmlForEdit: Node = XML.loadFile(xmlBackup)

      // Build each module and let it apply changes to XML
      val config = buildModules(modulesDir, modules, xmlForEdit)

      // Save the XML
      saveXml(xml, config)
    }
  }

  private def backupStandaloneXml(cfg: File): (File, File) = {
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
        tearDownAppServer()
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

  private def standaloneHaXmlPath(home: File) = new File(
    s"$home/standalone/configuration/standalone-ha.xml")

  private def standaloneXmlBackupFile(cfg: File): File =
    new File(s"${cfg.getCanonicalPath}.original")

}
