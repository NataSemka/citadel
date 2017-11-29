package org.natasemka.citadel.server.messages

import org.natasemka.citadel.model.{GameSession, Player}

trait CitadelMessage {
  val `type`: String
  val body: CitadelMessageBody
}

case class Credentials(playerId: String, password: String)

trait CitadelMessageBody

trait ServerMessage extends CitadelMessageBody
case class Authenticate(login: String, password: String) extends ServerMessage
case class Authenticated() extends ServerMessage
case class NotAuthenticated(reason: String) extends ServerMessage

case class JoinLobby(player: Player) extends ServerMessage
case class LobbyInfo(players: Seq[Player], games: Seq[GameSession]) extends ServerMessage

case class CreateGame(player: Player, game: GameSession) extends ServerMessage
case class JoinGame(player: Player, gameId: String) extends ServerMessage
case class LeaveGame(player: Player, gameId: String) extends ServerMessage



trait LobbyEvent extends CitadelMessageBody
case class PlayerJoinedLobby(player: Player) extends LobbyEvent
case class PlayerLeftLobby(player: Player) extends LobbyEvent
case class NewGameAvailable(game: GameSession) extends LobbyEvent
case class GameNoLongerAvailable(game: GameSession) extends LobbyEvent
case class ChatMessage(playerId: String, message: String, timestamp: Long) extends LobbyEvent

