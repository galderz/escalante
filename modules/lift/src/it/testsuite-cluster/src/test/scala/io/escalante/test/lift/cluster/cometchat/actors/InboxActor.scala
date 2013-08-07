package io.escalante.test.lift.cluster.cometchat.actors

import io.escalante.logging.Log
import io.escalante.test.lift.cluster.cometchat.service._
import net.liftweb.actor.LiftActor
import net.liftweb.common.Full
import net.liftweb.http.NamedCometListener
import net.liftweb.util.Helpers._
import net.liftweb.util.Schedule

object InboxActor extends LiftActor with Log {

  private[this] var since: BigInt = 0

  override def messageHandler ={
    case m @ Message(_,_,_)       => {
      info("We got Message %s" format  m)
      CentralChatServer.send(m)
    }
    case m @ MessageRow(_,_,_,_)  => {
      info("Got a MessageRow, sending it to comet dispatch")
      NamedCometListener.getDispatchersFor(Full("chat")).foreach(actor => actor.map(_ ! m))
    }
    case Since(s)                 => {
      if (s > 0) since = s
      info("scheduling readChangesFeed with since: %s." format since)
      Schedule.schedule(() => CentralChatServer.readChangesFeed(since),  seconds(1))
    }
  }

}
