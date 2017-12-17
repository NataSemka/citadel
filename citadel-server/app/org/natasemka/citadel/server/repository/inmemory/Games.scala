package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.GameSession
import org.natasemka.citadel.server.repository.api.GameRepo

import scala.collection.mutable

object Games extends RepoWithOptId[Long, GameSession] with GameRepo {
  private val byUser = mutable.Map[String, Long]()
  private val users = mutable.Map[Long, mutable.Set[String]]()

  override def delete(id: Long): Either[Exception, Boolean] = {
    users.get(id) match {
      case Some(usersInGame) => usersInGame.map(byUser.remove(_))
      case _ => Seq.empty
    }
    super.delete(id)
  }

  override def join(userId: String, gameId: Long): Either[Exception, GameSession] =
    entities.get(gameId) match {
      case Some(game) =>
        if (game.players.lengthCompare(game.playerCapacity) < 0) {
          byUser.put(userId, gameId)
          users.get(gameId) match {
            case Some(usersInGame) => usersInGame + userId
            case _ =>
              val usersInGame = mutable.LinkedHashSet(userId)
              users.put(gameId, usersInGame)
          }
          Right(game)
        } else Left(new RuntimeException("Game session is full"))
      case _ => Left(new RuntimeException("Game does not exist"))
    }

  override def leave(userId: String): Integer =
    byUser.get(userId) match {
      case Some(gameId) =>
        byUser.remove(userId)
        users.get(gameId) match {
          case Some(usersInGame) =>
            usersInGame - userId
            usersInGame.size
          case _ => 0
        }
      case _ => 0
    }

  override def ofUser(userId: String): Option[GameSession] =
    byUser.get(userId) match {
      case Some(gameId) => entities.get(gameId)
      case _ => None
    }
}
