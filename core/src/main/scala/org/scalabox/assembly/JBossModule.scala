package org.scalabox.assembly

import java.io.File

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
trait JBossModule {

   def build(destDir: File)

//   /**
//    * Build a
//    *
//    * @return
//    */
//   def buildJar: JavaArchive
//
//   /**
//    *
//    * @return
//    */
//   def moduleXml: Node

}
