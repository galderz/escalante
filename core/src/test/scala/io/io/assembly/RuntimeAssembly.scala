package io.escalante.assembly

import java.io.File
import io.escalante.logging.Log
import io.escalante.SCALA_292
import io.escalante.modules.JBossModulesRepository

/**
 * // TODO: Is this really needed? Why not just call LiftModule.build?
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
object RuntimeAssembly extends Log {

   def build(moduleInstallDir: File, home: File, module: EscalanteModule) {
      info("Build Lift extension and copy to container")

      // Install default Scala module
      val repo = new JBossModulesRepository(moduleInstallDir)
      repo.installScalaModule(SCALA_292)

      // Install given module
      module.build(moduleInstallDir)
   }

}
