/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.lift

import assembly.LiftModule
import io.escalante.util.FileSystem._
import io.escalante.util.JBossEnvironment._
import org.jboss.dmr.ModelNode
import org.jboss.as.controller.descriptions.ModelDescriptionConstants._
import io.escalante.logging.Log
import org.scalatest.junit.AssertionsForJUnit
import java.io.{FileOutputStream, File}
import io.escalante.assembly.RuntimeAssembly
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset

/**
 * Sets up a Lift test
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
abstract class LiftTestSetup {

   // To avoid being instantiated by the surefire...

}

object LiftTestSetup extends AssertionsForJUnit with Log {

   private def jbossHome = new File(
      System.getProperty("surefire.basedir", ".") + "/build/target/jboss-as")

   def installExtension() {
      info("Build Lift extension and copy to container")
      val home = jbossHome

      // Cleanup thirdparty deployments module dir, if present
      deleteDirectoryIfPresent(new File(home, "thirdparty-modules"))

      // Set up Lift module
      val tmpFile = new File(System.getProperty("java.io.tmpdir"))
      // Delete if present!
      val modulesInstallDir = mkDirs(tmpFile, "test-module", deleteIfPresent = true)
      info("Build Lift module into %s", modulesInstallDir)

      // Run assembly build
      RuntimeAssembly.build(modulesInstallDir, home, LiftModule)

      // Backup standalone configuration
      backupStandaloneXml(home)

      // Copy test standalone xml to server
      copy(new ClassLoaderAsset("standalone.xml").openStream(),
            new FileOutputStream(standaloneXml(home)))
   }

   def uninstallExtension() {
      val stdCfg = standaloneXml(jbossHome)
      val stdCfgOriginal = new File("%s.original".format(stdCfg.getCanonicalPath))
      copy(stdCfgOriginal, stdCfg) // Restore original standalone config
   }

   def validateResponse(r: ModelNode): ModelNode = {
      val outcome = r.get(OUTCOME).asString()
      if (outcome == FAILED) {
         val failure = r.get(FAILURE_DESCRIPTION).asString
         if (failure.contains("Duplicate resource")) {
            // If duplicate resource found, it could be due to test not
            // having finished properly, so clean up before propagating failure
            info("Duplicate extension found, carry on...")
            uninstallExtension()
         }
         fail(r.toString)
         null
      } else {
         assert(SUCCESS === outcome)
         r.get(RESULT)
      }
   }

}