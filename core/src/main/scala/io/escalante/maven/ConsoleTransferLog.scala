/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package io.escalante.maven

import io.escalante.logging.Log
import org.sonatype.aether.transfer.{TransferResource, TransferEvent, AbstractTransferListener}
import java.util.concurrent.ConcurrentHashMap
import java.io.PrintStream
import org.sonatype.aether.transfer.TransferEvent.RequestType.PUT
import scala.collection.JavaConversions._

/**
 * Console Maven artifact download transfer log.
 *
 * @author Galder Zamarreño
 * @since 1.0
 */
class ConsoleTransferLog(out: PrintStream) extends AbstractTransferListener {

  import ConsoleTransferLog._

  // a map of transferred data sizes for the last notification
  val downloads = new ConcurrentHashMap[TransferResource, Long]()

  var lastLength = 0

  override def transferInitiated(event: TransferEvent) {
    val message =
      if (event.getRequestType == PUT) "Uploading" else "Downloading"

    val resource = event.getResource
    info("%s: %s%s", message, resource.getRepositoryUrl,
      resource.getResourceName)
  }

  override def transferProgressed(event: TransferEvent) {
    val resource = event.getResource
    downloads.put(resource, event.getTransferredBytes)

    val buffer = new java.lang.StringBuilder(64)

    asScalaIterator(downloads.entrySet().iterator()).foreach {
      entry =>
        val total = entry.getKey.getContentLength
        val complete = entry.getValue
        buffer.append(getStatus(complete.longValue(), total)).append("  ")
    }

    val padCharNum = lastLength - buffer.length()
    lastLength = buffer.length()
    pad(buffer, padCharNum)

    buffer.append('\r')
    out.print(buffer)
  }

  override def transferSucceeded(event: TransferEvent) {
    transferCompleted(event)
    super.transferSucceeded(event)
  }

  override def transferCorrupted(event: TransferEvent) {
    transferCompleted(event)
    warn(uncompletedTransferLogMessage("Corrupted", event))
    super.transferCorrupted(event)
  }

  override def transferFailed(event: TransferEvent) {
    transferCompleted(event)
    warn(uncompletedTransferLogMessage("Failed", event))
    super.transferFailed(event)
  }

  private def uncompletedTransferLogMessage(
    cause: String, event: TransferEvent): String = {
    // Event resource
    val resource = event.getResource
    val sb = new java.lang.StringBuilder().append(cause).append(
      if (event.getRequestType == PUT) " upload of " else " download of ")
      .append(resource.getResourceName)
      .append(if (event.getRequestType == PUT) " into " else " from ")
      .append(resource.getRepositoryUrl).append(", reason: ")
      .append(event.getException.toString)

    sb.toString
  }

  private def transferCompleted(event: TransferEvent) {
    downloads.remove(event.getResource)
    //      val buffer = new java.lang.StringBuilder(64)
    //      pad(buffer, lastLength)
    //      buffer.append('\r')
    //      out.print(buffer)
  }

  // converts into status message
  private def getStatus(complete: Long, total: Long): String = {
    if (total >= 1024)
      toKB(complete) + "/" + toKB(total) + " KB"
    else if (total >= 0)
      complete + "/" + total + " B"
    else if (complete >= 1024)
      toKB(complete) + " KB"
    else
      complete + " B"
  }

  private def toKB(bytes: Long): Long = (bytes + 1023) / 1024

  private def pad(buffer: java.lang.StringBuilder, spaces: Int) {
    var spacesLeft = spaces
    val block = "                                        "
    while (spacesLeft > 0) {
      val n = scala.math.min(spaces, block.length())
      buffer.append(block, 0, n)
      spacesLeft = spacesLeft - n
    }
  }

}

object ConsoleTransferLog extends Log {

  // No-op

}
