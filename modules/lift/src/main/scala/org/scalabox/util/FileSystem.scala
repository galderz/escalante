package org.scalabox.util

import collection.JavaConversions._
import java.io._
import org.scalabox.util.Closeable._
import java.net.URISyntaxException

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object FileSystem {

   def mkDirs(parent: File, child: String): File = mkDirs(parent, child, false)

   def mkDirs(parent: File, child: String, deleteIfPresent: Boolean): File = {
      val f = new File(parent, child)
      if (deleteIfPresent) deleteDirectory(f)
      f.mkdirs()
      f
   }

   /**
    * Returns the target directory
    */
   def getTarget: File = {
      try {
         new File(new File(
            this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getParent)
      } catch {
         case e: URISyntaxException => {
            throw new RuntimeException("Could not obtain the target URI", e)
         }
      }
   }

   def copy(srcPath: String, destPath: String) {
      copy(new File(srcPath), new File(destPath))
   }

   def copy(src: File,  dest: File) {
      if (src.isDirectory) { // if directory not exists, create it
         if (!dest.exists()) {
            dest.mkdir();
            println("Directory copied from %s to %s"
                  .format(src.getCanonicalPath, dest.getCanonicalPath))
         }
         // List all the directory contents
         asScalaIterator(src.list().iterator).foreach { file =>
            // Recursive copy
            copy(new File(src, file), new File(dest, file))
         }
      } else {
         copy(new FileInputStream(src), new FileOutputStream(dest))
         println("File copied from %s to %s"
               .format(src.getCanonicalPath, dest.getCanonicalPath))
      }
   }

   def copy(in: InputStream, out: OutputStream) {
      use(in) { in =>
         use(out) { out =>
            val buffer = new Array[Byte](1024)
            Iterator.continually(in.read(buffer))
               .takeWhile(_ != -1)
               .foreach { out.write(buffer, 0 , _) }
         }
      }
   }

   /**
    * Recursively deletes a directory and all its contents
    *
    * @param directory
    */
   def deleteDirectory(directory: File) {
      if (directory.isDirectory && directory.exists) {
         for (file <- directory.listFiles) {
            if (file.isDirectory) {
               deleteDirectory(file)
            } else {
               if (!file.delete) {
                  throw new RuntimeException(
                     "Failed to delete file: %s".format(file))
               }
            }
         }
         if (!directory.delete) {
            throw new RuntimeException(
               "Failed to delete directory: %s".format(directory))
         }
      }
      else {
         throw new RuntimeException(("Unable to delete directory: %s.  " +
               "It is either not a directory or does not exist.").format(directory))
      }
   }

}