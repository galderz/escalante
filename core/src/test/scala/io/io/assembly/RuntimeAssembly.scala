package io.escalante.assembly

import java.io.File
import io.escalante.logging.Log
import io.escalante.SCALA_291
import io.escalante.modules.JBossModulesRepository

/**
 * // TODO: Is this really needed? Why not just call LiftModule.build?
 *
 * @author Galder Zamarreño
 * @since // TODO
 */
object RuntimeAssembly extends Log {

   def build(destDir: File, module: EscalanteModule) {
      info("Build Lift extension and copy to container")

      module.build(destDir)

      // TODO: Pass this in the module build
      val repo = new JBossModulesRepository(destDir)

      repo.installScalaModule(SCALA_291)
   }

}
