/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.util

import java.io.File
import io.escalante.util.FileSystem._

/**
 * JBoss Application Server runtime constants.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object JBossEnvironment {

   def standaloneXml(home: File) = new File(
      "%s/standalone/configuration/standalone.xml".format(home))

   def backupStandaloneXml(home: File): (File, File) = {
      val cfg = standaloneXml(home)
      val cfgBackup = standaloneXmlBackupFile(cfg)
      if (!cfgBackup.exists())
         copy(cfg, cfgBackup) // Backup original standalone config

      (cfg, cfgBackup)
   }

   def restoreStandaloneXml(home: File) {
      val cfg = standaloneXml(home)
      val cfgBackup = standaloneXmlBackupFile(cfg)
      copy(cfgBackup, cfg) // Restore original standalone config
   }

   private def standaloneXmlBackupFile(cfg: File): File =
      new File("%s.original".format(cfg.getCanonicalPath))

}
