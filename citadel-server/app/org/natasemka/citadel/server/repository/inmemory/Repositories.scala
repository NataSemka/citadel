package org.natasemka.citadel.server.repository.inmemory

import com.google.inject.Singleton
import org.natasemka.citadel.server.repository.api._

@Singleton
case class InMemoryRepos() extends Repositories {
  override def users: UserRepo = Users
  override def games: GameRepo = Games
  override def chats: ChatRepo = Chats
}