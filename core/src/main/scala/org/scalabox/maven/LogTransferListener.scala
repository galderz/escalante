package org.scalabox.maven

import org.scalabox.logging.Log
import org.sonatype.aether.transfer.{TransferResource, TransferEvent, AbstractTransferListener}
import java.util.concurrent.ConcurrentHashMap
import java.text.{DecimalFormatSymbols, DecimalFormat}
import java.util.Locale

/**
 * TODO: Not very pretty, find a better one that mixes log and progress (screen...)
 * @author Galder Zamarreño
 * @since // TODO
 */
class LogTransferListener extends AbstractTransferListener {

   import LogTransferListener._

   // a map of transferred data sizes for the last notification
   val downloads = new ConcurrentHashMap[TransferResource, Long]()

   override def transferInitiated(event: TransferEvent) {
      val resource = event.getResource();

      info("%s:%s%s",
         if (event.getRequestType() == TransferEvent.RequestType.PUT)
            "Uploading"
         else "Downloading",
         resource.getRepositoryUrl, resource.getResourceName)

      downloads.put(resource, 0)
   }

   override def transferProgressed(event: TransferEvent) {
      val resource = event.getResource()

      val lastTransferred = downloads.get(resource)
      val transferred = event.getTransferredBytes()

      if (transferred - lastTransferred >= TRANSFER_THRESHOLD) {
         downloads.put(resource, transferred)
         val total = resource.getContentLength()
         info(getStatus(transferred, total) + ", ")
      }
   }

   override def transferCorrupted(event: TransferEvent) {
      val resource = event.getResource()
      downloads.remove(resource)

      val sb = new StringBuilder().append("Corrupted")
              .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
         " upload of "
      else " download of ")
              .append(resource.getResourceName())
              .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
         " into "
      else " from ")
              .append(resource.getRepositoryUrl()).append(", reason: ")
              .append(event.getException().toString())

      warn(sb.toString())
   }

   override def transferFailed(event: TransferEvent) {
      val resource = event.getResource()
      downloads.remove(resource)

      val sb = new StringBuilder().append("Failed")
              .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
         " uploading "
      else " downloading ")
              .append(resource.getResourceName())
              .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
         " into "
      else " from ")
              .append(resource.getRepositoryUrl()).append(", reason: ")
              .append(event.getException().toString())

      warn(sb.toString())
   }

   override def transferStarted(event: TransferEvent) {
      super.transferStarted(event)
   }

   override def transferSucceeded(event: TransferEvent) {
      val resource = event.getResource()
      downloads.remove(resource)
      val contentLength = event.getTransferredBytes()
      if (contentLength >= 0) {
         val duration = System.currentTimeMillis() - resource.getTransferStartTime()
         val kbPerSec = (contentLength / 1024.0) / (duration / 1000.0)

         val sb = new StringBuilder().append("Completed")
                 .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
            " upload of "
         else " download of ")
                 .append(resource.getResourceName())
                 .append(if (event.getRequestType() == TransferEvent.RequestType.PUT)
            " into "
         else " from ")
                 .append(resource.getRepositoryUrl()).append(", transferred ")
                 .append(if (contentLength >= 1024) toKB(contentLength) + " KB"
         else contentLength + " B").append(" at ")
                 .append(new DecimalFormat("0.0",
            new DecimalFormatSymbols(Locale.ENGLISH)).format(kbPerSec))
                 .append("KB/sec")

         info(sb.toString())
      }
   }

   // converts into status message
   def getStatus(complete: Long, total: Long): String = {
      if (total >= 1024)
         return toKB(complete) + "/" + toKB(total) + " KB"
      else if (total >= 0)
         complete + "/" + total + " B"
      else if (complete >= 1024)
         toKB(complete) + " KB"
      else
         complete + " B"
   }

   def toKB(bytes: Long): Long = (bytes + 1023) / 1024

}

object LogTransferListener extends Log {

   val TRANSFER_THRESHOLD = 1024 * 50

}