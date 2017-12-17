package org.natasemka.citadel.server.repository.api

import org.natasemka.citadel.model.GameSession

trait GameRepo extends Repository[Long, GameSession] {
  def join(userId: String, gameId: Long): Either[Exception, GameSession]
  def leave(userId: String): Integer
  def ofUser(userId: String): Option[GameSession]
}
