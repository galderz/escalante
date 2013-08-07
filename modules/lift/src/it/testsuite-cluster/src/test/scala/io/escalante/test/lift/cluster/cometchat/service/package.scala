package io.escalante.test.lift.cluster.cometchat

import org.joda.time.DateTime

package object service{
  case class MessageRow(dateTime: DateTime, user: String, message: String, server: String)
}
