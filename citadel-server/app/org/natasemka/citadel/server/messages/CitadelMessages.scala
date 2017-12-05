package org.natasemka.citadel.server.messages

import akka.actor.ActorRef
import org.natasemka.citadel.model._
import play.api.libs.json._

case class PackagedMessage[Body <: CitadelMessage](`type`: String, body: Body)

trait CitadelMessage

trait UserMessage extends CitadelMessage
case class Credentials(userId: String, password: String) extends UserMessage
case class Authenticate(login: String, password: String) extends UserMessage

trait ServerMessage extends CitadelMessage
case class Authenticated(user: User) extends ServerMessage
case class NotAuthenticated(reason: String) extends ServerMessage
// lobby messages
case class JoinLobby(user: User) extends ServerMessage
case class LobbyInfo(users: Seq[User], games: Seq[GameSession]) extends ServerMessage
// game session messages
case class CreateGame(user: User, game: GameSession) extends ServerMessage
case class JoinGame(user: User, gameId: String) extends ServerMessage
case class UserJoinedGame(user: User, gameId: String) extends ServerMessage
case class LeaveGame(user: User, gameId: String) extends ServerMessage
case class UserLeftGame(user: User, gameId: String) extends ServerMessage

trait LobbyEvent extends CitadelMessage
case class UserJoinedLobby(user: User) extends LobbyEvent
case class UserLeftLobby(user: User) extends LobbyEvent
case class NewGameAvailable(game: GameSession) extends LobbyEvent
case class GameNoLongerAvailable(game: GameSession) extends LobbyEvent

trait ChatEvent extends CitadelMessage
case class ChatMessage(chatId: String, userId: String, message: String, timestamp: Option[Long])
  extends ChatEvent with UserMessage
case class SubToChat(chatId: String, client: ActorRef)
case class UnsubFromChat(chatId: String, client: ActorRef)




object CitadelMessages {
  implicit val credentialsFormat: OFormat[Credentials] = Json.format[Credentials]
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val authenticatedFmt: OFormat[Authenticated] = Json.format[Authenticated]
  implicit val chatMessageFormat: OFormat[ChatMessage] = Json.format[ChatMessage]

  implicit object colorWrites extends Writes[Color] {
    override def writes(o: Color): JsValue = o match {
      case Blue => Json.toJson("blue")
      case Red => Json.toJson("red")
      case Green => Json.toJson("green")
      case Yellow => Json.toJson("yellow")
      case Purple => Json.toJson("purple")
      case _ => Json.toJson("unrecognized")
    }
  }
  implicit object colorReads extends Reads[Color] {
    override def reads(json: JsValue): JsResult[Color] = json match {
      case JsString("red") => JsSuccess(Red)
      case JsString("blue") => JsSuccess(Blue)
      case JsString("yellow") => JsSuccess(Yellow)
      case JsString("green") => JsSuccess(Green)
      case JsString("purple") => JsSuccess(Purple)
      case invalidColor => JsError(s"Unrecognized color: $invalidColor")
    }
  }
  //implicit val colorFmt: OFormat[Color] = Json.format[Color]
  implicit val actionFmt: OFormat[Action] = Json.format[Action]
  implicit val quarterPropFmt: OFormat[QuarterProperty] = Json.format[QuarterProperty]
  implicit val quarterFmt: OFormat[Quarter] = Json.format[Quarter]
  implicit val charFmt: OFormat[Character] = Json.format[Character]
  implicit val rulesFmt: OFormat[Rules] = Json.format[Rules]
  implicit val playerFmt: OFormat[Player] = Json.format[Player]

  implicit val charDeckFmt: OFormat[CharacterDeck] = Json.format[CharacterDeck]
  implicit val quarterDeck: OFormat[QuarterDeck] = Json.format[QuarterDeck]
  implicit val roundFmt: OFormat[Round] = Json.format[Round]
  implicit val gameSessionFmt: OFormat[GameSession] = Json.format[GameSession]


  implicit val lobbyInfoFmt: OFormat[LobbyInfo] = Json.format[LobbyInfo]
  implicit val userJoinedLobbyFormat: OFormat[UserJoinedLobby] = Json.format[UserJoinedLobby]

  implicit def packagedMessageFmt[T <: CitadelMessage](implicit fmt: OFormat[T]): OFormat[PackagedMessage[T]] =
    new OFormat[PackagedMessage[T]] {

      override def writes(o: PackagedMessage[T]): JsObject =
        JsObject(Seq("type" -> JsString(o.`type`), "body" -> Json.toJson(o.body)))

      override def reads(json: JsValue): JsResult[PackagedMessage[T]] = {
        (json \ "type", json \ "body") match {
          case (JsDefined(JsString(msgType)), JsDefined(bodyJson)) =>
            Json.fromJson[T](bodyJson) match {
              case JsSuccess(body, _) => JsSuccess(PackagedMessage(msgType, body))
              case _ => JsError("Unrecognized message body format")
            }
          case _ => JsError("Unrecognized message format")
        }
      }
    }

  implicit def packagedUserJoinedLobby(msg: UserJoinedLobby): PackagedMessage[UserJoinedLobby] =
    PackagedMessage("UserJoinedLobby", msg)
}