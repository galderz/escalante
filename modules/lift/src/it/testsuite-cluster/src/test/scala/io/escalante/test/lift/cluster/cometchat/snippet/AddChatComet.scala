package io.escalante.test.lift.cluster.cometchat.snippet

import net.liftweb.http.NamedCometActorSnippet

class AddChatComet extends NamedCometActorSnippet{
  def name = "chat"
  def cometClass = "ChatClient"
}
