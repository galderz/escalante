package io.escalante.test.lift.cluster.cometchat.service

import io.escalante.logging.Log
import io.escalante.test.lift.cluster.cometchat.actors._
import net.liftweb.http.ContainerVar
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
import org.joda.time.format.DateTimeFormat

object CentralChatServer extends Log{

  object ChatMessages extends ContainerVar[List[String]](List())

  /**
   * Send our payload to the chat server so that then the other datacenters can
   * read the new entry and broadcast the new message.
   */
  def send(m: Message) {
    val j = ("user" -> m.user) ~
      ("msg" -> m.msg) ~
      ("datetime" -> m.dateTime.toString("YYY MMM dd hh:mm:ss ZZ")) ~
      ("host" -> java.net.InetAddress.getLocalHost.getHostName)

    ChatMessages(compact(render(j)) +: ChatMessages.is)
  }

  def readChangesFeed(since: BigInt = 0) {
    val messages: List[MessageRow] = for {
      jsonMessage <- ChatMessages
      JObject(child) <- parse(jsonMessage)
      JField("user", JString(username)) <- child
      JField("msg", JString(message)) <- child
      JField("datetime", JString(datetime)) <- child
      JField("host", JString(hostname)) <- child
    } yield {
      val fmt = DateTimeFormat.forPattern("YYY MMM dd hh:mm:ss ZZ")
      MessageRow(fmt.parseDateTime(datetime), username, message, hostname)
    }

    info("Messages received are: " + messages)
    messages.map(InboxActor ! )
  }

}
