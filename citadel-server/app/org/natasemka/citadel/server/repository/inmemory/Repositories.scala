package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.{GameSession, User, WithId, WithOptId}
import org.natasemka.citadel.server.repository.api.Repository

object Repositories {
  implicit def longKeyGen: KeyGen[Long] = new KeyGen[Long] {
    var counter: Long = -1
    override def next: Long = {
      counter = counter + 1
      counter
    }
  }

  def repoWithId[K,V <: WithId[K]]: Repository[K,V] =
    new RepoWithId[K, V]()

  def repoWithOptId[V <: WithOptId[Long, V]]: Repository[Long, V] =
    new RepoWithOptId[Long, V]()

  val Users: Repository[String, User] = repoWithId[String, User]
  val GameSessions: Repository[Long, GameSession] = repoWithOptId[GameSession]
}