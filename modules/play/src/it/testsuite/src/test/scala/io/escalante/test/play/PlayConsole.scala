/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play

import java.io._
import java.lang.InterruptedException
import org.jboss.as.protocol.StreamUtils
import io.escalante.logging.Log
import java.net.{HttpURLConnection, URL}
import io.escalante.io.FileSystem
import io.escalante.play.Play

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object PlayConsole extends Log {

//  def main(args: Array[String]) {
//    packageApp(new File("/Users/g/Go/code/escalante.git/modules/play/src/it/testsuite/src/test/applications/persistdb"))
//  }

  def packageApp(appPath: File) {
    // TODO: Rather the executing a process, could SBT be called programmatically?
    // Looks doable, but you need to get access to the Ivy repository where SBT
    // is released (i.e. http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/launcher/),
    // for which the ivy-maven-plugin is needed: http://evgeny-goldin.com/wiki/Ivy-maven-plugin

    val playExec = unzipPlay()
    execute(List(playExec.getAbsolutePath, "package"), appPath)
  }

  private def unzipPlay(): File = {
    val playVersion = Play().version
    val tmp = System.getProperty("java.io.tmpdir")
    val playTarget = new File(tmp, s"play-$playVersion")
    val playExecutable = new File(playTarget, "play")
    val playZipTarget = new File(tmp, s"play-$playVersion.zip")
    // Download if zip not present
    if (!playZipTarget.exists()) {
      debug("Download Play 2.1.1 distribution for testing")
      val url = new URL(s"http://downloads.typesafe.com/play/$playVersion/play-$playVersion.zip")
      val connection = url.openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")
      FileSystem.copy(connection.getInputStream, new FileOutputStream(playZipTarget))
    }
    // Unzip if not expanded
    if (!playExecutable.exists()) {
      debug("Unzip Play distribution")
      FileSystem.unzip(playZipTarget, new File(tmp))
      playExecutable.setExecutable(true)
      // Make other files executable
      new File(playTarget, "framework/build").setExecutable(true)
    }

    playExecutable
  }

  private def execute(args: Seq[String], appPath: File) {
    val fullCommand = "$ " + args.mkString(" ")
    info(s"Execute: [$fullCommand] in $appPath")
    val builder = new ProcessBuilder(args : _*)
    builder.redirectErrorStream(true)
    builder.directory(appPath)
    val process = builder.start()
    val stdout = process.getInputStream
    val consoleConsumer = new ConsoleConsumer(stdout, System.out)
    val outThread = new Thread(consoleConsumer, "Play Console consumer")
    outThread.start()

    try {
      process.waitFor()
    } catch {
      case _: InterruptedException => process.destroy()
    }

    // This ensures that all output is complete
    // before returning (waitFor does not ensure this)
    outThread.join()

    if (process.exitValue() != 0)
      throw new Exception(s"[$fullCommand] execution failed in $appPath")
  }

  private class ConsoleConsumer(
      source: InputStream, target: PrintStream) extends Runnable {
    def run() {
      val source = this.source
      try {
        val buf = new Array[Byte](32)
        // Do not try reading a line cos it considers '\r' end of line
        var reading = true
        while (reading) {
          source.read(buf) match {
            case -1 => reading = false
            case c => target.write(buf, 0, c)
          }
        }
      } catch {
        case e: IOException =>
          e.printStackTrace(target)
      } finally {
        StreamUtils.safeClose(source)
      }
    }
  }

}
