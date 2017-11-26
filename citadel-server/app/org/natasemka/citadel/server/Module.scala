package org.natasemka.citadel.server

import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  import org.natasemka.citadel.server.actors._

  override def configure(): Unit = {
    bindActor[LobbyActor]("lobbyActor")
    bindActor[SessionManager]("sessionManager")
    bindActor[CitadelManager]("citadelManager")
    bindActorFactory[ClientSocket, ClientSocket.Factory]
  }
}