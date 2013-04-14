/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.escalante.test.play

import org.jboss.shrinkwrap.descriptor.api.Descriptor
import java.io.{File, ByteArrayInputStream, InputStream, OutputStream}
import java.nio.charset.Charset

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
class PlayDescriptor(
    deployName: String,
    appName: String,
    withDb: Boolean) extends Descriptor {

  val appPath: File = calculateAppPath(appName)

  def exportAsString(): String = {
    if (withDb) {
      s"""
      | play:
      |   path: $appPath
      |   modules:
      |     - play-jdbc
      |     - anorm
      """.stripMargin
    } else {
      s"""
      | play:
      |   path: $appPath
      """.stripMargin
    }
  }

  def exportTo(output: OutputStream) {
    output.write(exportAsByteArray)
  }

  def getDescriptorName: String = deployName

  def exportAsStream: InputStream = new ByteArrayInputStream(exportAsByteArray)

  private def exportAsByteArray: Array[Byte] =
    exportAsString().getBytes(Charset.forName("UTF-8"))

  private def calculateAppPath(appName: String): File = {
    val commandLinePath = s"src/test/applications/$appName"
    val ideFriendlyPath = s"modules/play/src/it/testsuite/$commandLinePath"
    val ideFriendlyFile = new File(ideFriendlyPath)
    if (ideFriendlyFile.exists())
      ideFriendlyFile.getAbsoluteFile
    else
      new File(commandLinePath).getAbsoluteFile
  }

}
