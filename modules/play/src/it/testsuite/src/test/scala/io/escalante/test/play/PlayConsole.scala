/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play

import java.io.{IOException, PrintStream, InputStream, File}
import java.lang.InterruptedException
import org.jboss.as.protocol.StreamUtils
import io.escalante.logging.Log

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
object PlayConsole extends Log {

  def packageApp(appPath: String) {
    // TODO: Rather the executing a process, could SBT be called programmatically?
    // Looks doable, but you need to get access to the Ivy repository where SBT
    // is released (i.e. http://repo.typesafe.com/typesafe/ivy-releases/org.scala-sbt/launcher/),
    // for which the ivy-maven-plugin is needed: http://evgeny-goldin.com/wiki/Ivy-maven-plugin

    // TODO: Avoid reliance on user environment, i.e. unzip play distro to tmp?
//    execute(List("/opt/play/play", "clean", "package"), appPath)
    execute(List("/opt/play/play", "package"), appPath)
  }

  private def execute(args: Seq[String], appPath: String) {
    val fullCommand = "$ " + args.mkString(" ")
    info(s"Execute: [$fullCommand] in $appPath")
    val builder = new ProcessBuilder(args : _*)
    builder.redirectErrorStream(true)
    // builder.environment().putAll(env)
    builder.directory(new File(appPath).getAbsoluteFile)
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
