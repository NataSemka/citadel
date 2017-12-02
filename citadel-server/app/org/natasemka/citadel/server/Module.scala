package org.natasemka.citadel.server

import com.google.inject.AbstractModule
import org.natasemka.citadel.server.actors.{CitadelManager, ClientSocket}
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    bindActor[CitadelManager]("citadelManager")
    bindActorFactory[ClientSocket, ClientSocket.Factory]
  }
}