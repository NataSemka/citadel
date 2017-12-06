package org.natasemka.citadel.server.messages

import akka.actor.ActorRef
import org.natasemka.citadel.model._
import org.natasemka.citadel.server.messages.JsonMessages._
import play.api.libs.json._

case class PackagedMessage[+T <: CitadelMessage](`type`: String, body: T) {
  import CitadelMessages.packagedMessageFmt
  def toJson: String = Json.stringify(Json.toJson(this))
}

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
  implicit val chatMessageFmt: OFormat[ChatMessage] = Json.format[ChatMessage]

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

  implicit val quarterPropFmt: OFormat[QuarterProperty] = Json.format[QuarterProperty]
  implicit val quarterFmt: OFormat[Quarter] = Json.format[Quarter]
  implicit val charFmt: OFormat[Character] = Json.format[Character]
  implicit val rulesFmt: OFormat[Rules] = Json.format[Rules]
  implicit val playerFmt: OFormat[Player] = Json.format[Player]
  implicit val actionFmt: OFormat[Action] = Json.format[Action]

  implicit val charDeckFmt: OFormat[CharacterDeck] = Json.format[CharacterDeck]
  implicit val quarterDeck: OFormat[QuarterDeck] = Json.format[QuarterDeck]
  implicit val roundFmt: OFormat[Round] = Json.format[Round]
  implicit val gameSessionFmt: OFormat[GameSession] = Json.format[GameSession]


  implicit val lobbyInfoFmt: OFormat[LobbyInfo] = Json.format[LobbyInfo]
  implicit val userJoinedLobbyFmt: OFormat[UserJoinedLobby] = Json.format[UserJoinedLobby]

  implicit def packagedMessageFmt: OFormat[PackagedMessage[CitadelMessage]] =
    new OFormat[PackagedMessage[CitadelMessage]] {
      override def writes(o: PackagedMessage[CitadelMessage]): JsObject = {
        implicit val pkgd: PackagedMessage[CitadelMessage] = o
        o.body match {
          case _: Authenticated => write(authenticatedFmt)
          case _: LobbyInfo => write(lobbyInfoFmt)
          case _: UserJoinedLobby => write(userJoinedLobbyFmt)
          case _: ChatMessage => write(chatMessageFmt)
          case _ => throw new RuntimeException(s"Unrecognized CitadelMessage: $o")
        }
      }

      def write[T <: CitadelMessage](format: OFormat[T])
                                    (implicit pkgd: PackagedMessage[CitadelMessage]): JsObject =
        JsObject(Seq(
          "type" -> JsString(pkgd.`type`),
          "body" -> format.writes(pkgd.body.asInstanceOf[T])
        ))

      override def reads(json: JsValue): JsResult[PackagedMessage[CitadelMessage]] = {
        (json \ "type", json \ "body") match {
          case (JsDefined(JsString(msgType)), JsDefined(bodyJson)) =>
            implicit val iType: String = msgType
            implicit val iBody: JsValue = bodyJson
            msgType match {
              case SignInMsg => packageBody(credentialsFormat)
              case ChatMsg => packageBody(chatMessageFmt)
              //packageBody(fromJson[Credentials](bodyJson))
              //case JoinGameMsg => packageBody(fromJson[JoinGame](bodyJson))
              //              case ChatMsg => packageBody(fromJson[ChatMessage](bodyJson))
              case _ => JsError(s"Unrecognized message type: $msgType")
            }
          case _ => JsError("Unrecognized message format")
        }
      }

      def packageBody[T <: CitadelMessage]
        (reads: Reads[T])
        (implicit msgType: String, bodyJson: JsValue)
      : JsResult[PackagedMessage[T]] =
        Json.fromJson(bodyJson)(reads) match {
          //case JsSuccess(body, _) => JsSuccess(PackagedMessage(msgType, body))
          case JsSuccess(body, _) => JsSuccess(PackagedMessage(msgType, body))
          case error: JsError => error

      }

    }

  implicit def toPackagedMessage[T <: CitadelMessage](msg: T): PackagedMessage[T] = {
    def pack(msgType: String)
            (implicit body: T): PackagedMessage[T] =
      PackagedMessage(msgType, body)

    implicit val body: T = msg
    msg match {
      case _: Authenticate => pack(AuthenticateMsg)
      case _: Authenticated => pack(AuthenticatedMsg)
      case _: LobbyInfo => pack(LobbyInfoMsg)
      case _: UserJoinedLobby => pack(UserJoinedLobbyMsg)
      case _: ChatMessage => pack(ChatMsg)
      //case _ => Rejected
    }
  }

}