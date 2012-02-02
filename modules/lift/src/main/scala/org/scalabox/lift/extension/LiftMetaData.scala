package org.scalabox.lift.extension

import org.jboss.as.server.deployment.AttachmentKey

/**
 * // TODO: Document this
 * @author Galder Zamarreño
 * @since // TODO
 */
case class LiftMetaData(liftVersion: LiftVersion, scalaVersion: ScalaVersion)

object LiftMetaData {

   val ATTACHMENT_KEY = AttachmentKey.create(classOf[LiftMetaData])

}