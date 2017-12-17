package org.natasemka.citadel.server

import com.google.inject.AbstractModule
import org.natasemka.citadel.server.actors.{CitadelManager, ClientSocket}
import org.natasemka.citadel.server.repository.api.Repositories
import org.natasemka.citadel.server.repository.inmemory.InMemoryRepos
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[Repositories])
      .to(classOf[InMemoryRepos])
    bindActor[CitadelManager]("citadelManager")
    bindActorFactory[ClientSocket, ClientSocket.Factory]
  }
}