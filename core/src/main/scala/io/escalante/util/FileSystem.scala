/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.util

import collection.JavaConversions._
import java.io._
import io.escalante.util.Closeable._
import java.net.URISyntaxException
import io.escalante.logging.Log
import java.util.jar.JarFile

/**
 * Filesystem related utility methods
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
object FileSystem extends Log {

   /**
    * Make a directory.
    *
    * @param parent File instance representing the parent directory
    * @param child name of child directory to create
    * @return a File composed of the parent and child
    */
   def mkDirs(parent: File, child: String): File =
         mkDirs(parent, child, deleteIfPresent = false)

   /**
    * Make a directory, deleting it first if already present.
    *
    * @param parent File instance representing the parent directory
    * @param child name of child directory to create
    * @param deleteIfPresent if true, directory must be deleted before
    *                        recreating it
    * @return a File composed of the parent and child
    */
   def mkDirs(parent: File, child: String, deleteIfPresent: Boolean): File = {
      val f = new File(parent, child)
      if (deleteIfPresent & f.exists()) deleteDirectory(f)
      f.mkdirs()
      f
   }

   /**
    * Returns the target directory
    */
   def getTarget(clazz: Class[_]): File = {
      try {
         new File(new File(clazz.getProtectionDomain
               .getCodeSource.getLocation.toURI).getParent)
      } catch {
         case e: URISyntaxException => {
            throw new RuntimeException("Could not obtain the target URI", e)
         }
      }
   }

   def copy(srcPath: String, destPath: String) {
      copy(new File(srcPath), new File(destPath))
   }

   def copy(src: File, dest: File) {
      if (src.isDirectory) {
         // If directory not exists, create it
         if (!dest.exists()) {
            dest.mkdir()
            debug("Directory copied from %s to %s",
               src.getCanonicalPath, dest.getCanonicalPath)
         }
         // List all the directory contents
         asScalaIterator(src.list().iterator).foreach { file =>
            // Recursive copy
            copy(new File(src, file), new File(dest, file))
         }
      } else {
         copy(new FileInputStream(src), new FileOutputStream(dest))
         debug("File copied from %s to %s",
            src.getCanonicalPath, dest.getCanonicalPath)
      }
   }

   def copy(in: InputStream, out: OutputStream) {
      use(in) { in =>
         use(out) { out =>
            val buffer = new Array[Byte](1024)
            Iterator.continually(in.read(buffer))
               .takeWhile(_ != -1)
               .foreach {
                  out.write(buffer, 0, _)
               }
         }
      }
   }

   /**
    * Recursively deletes a directory and all its contents.
    *
    * @param directory File representing the directory to delete
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
               "It is either not a directory or does not exist.")
               .format(directory))
      }
   }

   def deleteDirectoryIfPresent(directory: File) {
      if (directory.exists()) deleteDirectory(directory)
   }

   def unzip(file: File, target: File) {
      val zip = new JarFile(file)
      enumerationAsScalaIterator(zip.entries).foreach { entry =>
         val entryPath = entry.getName
         println("Extracting to " + target.getCanonicalPath + "/" + entryPath)
         if (entry.isDirectory) {
            new File(target, entryPath).mkdirs
         } else {
            copy(zip.getInputStream(entry),
               new FileOutputStream(new File(target, entryPath)))
         }
      }
   }

   def fileToString(file: File, encoding: String): String = {
      val inStream = new FileInputStream(file)
      val outStream = new ByteArrayOutputStream
      try {
         var reading = true
         while ( reading ) {
            inStream.read() match {
               case -1 => reading = false
               case c => outStream.write(c)
            }
         }
         outStream.flush()
      }
      finally {
         inStream.close()
      }
      new String(outStream.toByteArray, encoding)
   }

   def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
      val p = new java.io.PrintWriter(f)
      try { op(p) } finally { p.close() }
   }

}