package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.GameSession
import org.natasemka.citadel.server.repository.api.GameRepo

import scala.collection.mutable

object Games extends RepoWithOptId[Long, GameSession] with GameRepo {
  private val byUser = mutable.Map[String, Long]()
  private val users = mutable.Map[Long, mutable.Set[String]]()
  private val pendingGames = mutable.Set[Long]()

  override protected def toChatId(counter: Long, entity: GameSession): Long =
    counter

  override def delete(id: Long): Boolean = {
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

  override def leave(userId: String): Option[GameSession] =
    byUser.get(userId) match {
      case Some(gameId) =>
        byUser.remove(userId)
        users.get(gameId).foreach(_ - userId)
        get(gameId)
      case _ => None
    }

  override def ofUser(userId: String): Option[GameSession] =
    byUser.get(userId) match {
      case Some(gameId) => entities.get(gameId)
      case _ => None
    }

  override def pending: Seq[GameSession] =
    pendingGames.map(get(_)).filter(_.isDefined).map(_.get).toSeq
}
