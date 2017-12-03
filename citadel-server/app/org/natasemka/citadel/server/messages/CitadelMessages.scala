package org.natasemka.citadel.server.messages

import org.natasemka.citadel.model.{GameSession, User}
import play.api.libs.json.{Json, OFormat}

trait PackagedMessage {
  val `type`: String
  val body: CitadelMessage
}

trait CitadelMessage

trait ServerMessage extends CitadelMessage
case class Credentials(userId: String, password: String) extends ServerMessage
case class Authenticate(login: String, password: String) extends ServerMessage
case class Authenticated() extends ServerMessage
case class NotAuthenticated(reason: String) extends ServerMessage

case class JoinLobby(user: User) extends ServerMessage
case class LobbyInfo(users: Seq[User], games: Seq[GameSession]) extends ServerMessage

case class CreateGame(user: User, game: GameSession) extends ServerMessage
case class JoinGame(user: User, gameId: String) extends ServerMessage
case class LeaveGame(user: User, gameId: String) extends ServerMessage



trait LobbyEvent extends CitadelMessage
case class UserJoinedLobby(user: User) extends LobbyEvent
case class UserLeftLobby(user: User) extends LobbyEvent
case class NewGameAvailable(game: GameSession) extends LobbyEvent
case class GameNoLongerAvailable(game: GameSession) extends LobbyEvent
case class ChatMessage(userId: String, message: String, timestamp: Long) extends LobbyEvent



object CitadelMessages {
  implicit val credentialsFormat: OFormat[Credentials] = Json.format
}