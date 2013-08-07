package io.escalante.test.lift.cluster.cometchat

import net.liftweb.common.Box
import org.joda.time.DateTime

package object actors {
  case object GetMessages
  case class RemoveMessages(l: List[(Box[String], Box[String])])
  case class AddMessages(user: Box[String], m: Box[String])

  case class Message(user: String, msg: String, dateTime: DateTime)
  case class Since(s: BigInt)

}
