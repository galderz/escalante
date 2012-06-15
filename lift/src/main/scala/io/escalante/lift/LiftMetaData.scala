package io.escalante.lift

import org.jboss.as.server.deployment.AttachmentKey
import io.escalante.Scala

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class LiftMetaData(liftVersion: Lift, scalaVersion: Scala)

object LiftMetaData {

   val ATTACHMENT_KEY = AttachmentKey.create(classOf[LiftMetaData])

}