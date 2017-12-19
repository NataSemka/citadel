package org.natasemka.citadel.server.messages

import akka.actor.ActorRef
import org.natasemka.citadel.model._
import org.natasemka.citadel.server.messages.JsonMessages._
import play.api.libs.json._

case class PackagedMessage[+T <: CitadelMessage](`type`: String, body: T) {
  import CitadelMessages._
  def packageJson: String = Json.stringify(Json.toJson(this))
}

trait CitadelMessage {
  def requestTitle: String = this match {
    case _: Credentials => SignInMsg
    case _: ServerError => ServerErrorMsg
    case _: Rejected => RejectedMsg

    case _: Authenticated => AuthenticatedMsg

    case _: ChatMessage => ChatMsg
    case _: ChatInfo => ChatInfoMsg
    case _: UserJoinedChat => UserJoinedChatMsg
    case _: UserLeftChat => UserLeftChatMsg

    case _: CreateGame => CreateGameMsg
    case _: JoinGame => JoinGameMsg
    case _: LeaveGame => LeaveGameMsg
    case _: UserJoinedGame => UserJoinedGameMsg
    case _: UserLeftGame => UserLeftGameMsg
    case _: AvailableGames => AvailableGamesMsg

    case _ => "Undefined"
  }
}

trait UserMessage extends CitadelMessage
case class Credentials(userId: String, password: String) extends UserMessage

trait ServerMessage extends CitadelMessage
case class Authenticated(user: User) extends ServerMessage
case class NotAuthenticated(reason: String) extends ServerMessage

case class ServerError(name: String, reasons: Seq[String]) extends ServerMessage
case class Rejected(title: String, request: String, reasons: Seq[String]) extends ServerMessage
case object Rejected {
  def apply(title: String, msg: CitadelMessage, reason: String): Rejected =
    apply(title, msg.requestTitle, reason)

  def apply(title: String, request: String, reason: String): Rejected =
    Rejected(title, request, Seq(reason))

  def internalServerError(msg: CitadelMessage, reason: String): Rejected =
    apply("Internal Server Error", msg, reason)

  def invalidRequest(msg: Any): Rejected =
    invalidJson("Undefined", s"Invalid message format: $msg")

  def notAuthenticated(msg: CitadelMessage): Rejected =
    notAuthenticated(msg, "Not authenticated, sign in first")

  def notAuthenticated(msg: CitadelMessage, reason: String) =
    apply("Not Authenticated", msg, reason)

  def notAuthorized(msg: CitadelMessage): Rejected =
    notAuthorized(msg.requestTitle, "Session user id doesn't match request id")

  def notAuthorized(msg: CitadelMessage, reason: String): Rejected =
    notAuthorized(msg.requestTitle, reason)

  def notAuthorized(request: String, reason: String): Rejected =
    apply("Not Authorized", request, reason)

  def undefined(reason: String) =
    invalidJson("Undefined", reason)

  def invalidJson(request: String): Rejected =
    invalidJson(request, "Received a message that is not a valid JSON")

  def invalidJson(request: String, reason: String): Rejected =
    invalidJson(request, Seq(reason))

  def invalidJson(request: String, reasons: Seq[String]): Rejected =
    apply("Invalid JSON", request, reasons)

}

// lobby messages
case class JoinLobby(user: User) extends ServerMessage

// game session messages
trait UserIdMessage extends UserMessage {
  def userId: String
}
case class CreateGame(userId: String, name: Option[String], rules: Option[RuleSet]) extends UserIdMessage
case class JoinGame(userId: String, gameId: String) extends UserIdMessage
case class LeaveGame(userId: String) extends UserIdMessage
case class UserJoinedGame(user: User, gameId: String) extends ServerMessage
case class UserLeftGame(user: User, gameId: String) extends ServerMessage
case class AvailableGames(games: Seq[GameSession]) extends ServerMessage

trait LobbyEvent extends CitadelMessage
case class NewGameAvailable(game: GameSession) extends LobbyEvent
case class GameNoLongerAvailable(game: GameSession) extends LobbyEvent

trait ChatEvent extends CitadelMessage
case class ChatInfo(chat: Chat, users: Seq[User]) extends ChatEvent
case class UserJoinedChat(user: User, chatId: String) extends ChatEvent
case class UserLeftChat(user: User, chat: Chat) extends ChatEvent
case class ChatMessage(userId: String, chatId: String, message: String, timestamp: Option[Long])
  extends ChatEvent with UserIdMessage
object ChatMessage {
  def noSuchChat(chatId: String): Rejected =
    Rejected("No Such Chat", ChatMsg, s"Chat $chatId does not exist")
}
case class SubToChat(chatId: String, client: ActorRef)
case class UnsubFromChat(chatId: String, client: ActorRef)



object CitadelMessages {
  implicit val serverErrorFmt: OFormat[ServerError] = Json.format[ServerError]
  implicit val rejectedFmt: OFormat[Rejected] = Json.format[Rejected]

  implicit val credentialsFormat: OFormat[Credentials] = Json.format[Credentials]
  implicit val userFormat: OFormat[User] = Json.format[User]
  implicit val authenticatedFmt: OFormat[Authenticated] = Json.format[Authenticated]
  implicit val chatMessageFmt: OFormat[ChatMessage] = Json.format[ChatMessage]



  implicit object chatTypeWrites extends Writes[ChatType] {
    override def writes(o: ChatType): JsValue = Json.toJson(o.toString)
  }

  implicit object chatTypeReads extends Reads[ChatType] {
    override def reads(json: JsValue): JsResult[ChatType] = json match {
      case JsString(id) => JsSuccess(ChatType.of(id))
      case invalidType => JsError(s"Unrecognized chat type: $invalidType")
    }
  }

  implicit val chatFmt: OFormat[Chat] = Json.format[Chat]
  implicit val userJoinedChatFmt: OFormat[UserJoinedChat] = Json.format[UserJoinedChat]
  implicit val chatInfoFmt: OFormat[ChatInfo] = Json.format[ChatInfo]



  implicit object colorWrites extends Writes[Color] {
    override def writes(o: Color): JsValue = Json.toJson(o.id)
  }

  implicit object colorReads extends Reads[Color] {
    override def reads(json: JsValue): JsResult[Color] = json match {
      case JsString(id) => JsSuccess(Color.of(id))
      case invalidColor => JsError(s"Unrecognized color: $invalidColor")
    }
  }

  implicit val quarterPropFmt: OFormat[QuarterProperty] = Json.format[QuarterProperty]
  implicit val quarterFmt: OFormat[Quarter] = Json.format[Quarter]
  implicit val actionFmt: OFormat[Action] = Json.format[Action]
  implicit val charFmt: OFormat[Character] = Json.format[Character]
  implicit val rulesFmt: OFormat[RuleSet] = Json.format[RuleSet]
  implicit val playerFmt: OFormat[Player] = Json.format[Player]

  implicit val charDeckFmt: OFormat[CharacterDeck] = Json.format[CharacterDeck]
  implicit val quarterDeck: OFormat[QuarterDeck] = Json.format[QuarterDeck]
  implicit val roundFmt: OFormat[Round] = Json.format[Round]
  implicit val gameSessionFmt: OFormat[GameSession] = Json.format[GameSession]

  implicit val availableGamesFmt: OFormat[AvailableGames] = Json.format[AvailableGames]

  implicit def packagedMessageFmt: OFormat[PackagedMessage[CitadelMessage]] =
    new OFormat[PackagedMessage[CitadelMessage]] {
      override def writes(o: PackagedMessage[CitadelMessage]): JsObject = {
        implicit val pkgd: PackagedMessage[CitadelMessage] = o
        o.body match {
          case _: ServerError => write(serverErrorFmt)
          case _: Rejected => write(rejectedFmt)
          case _: Authenticated => write(authenticatedFmt)
          case _: UserJoinedChat => write(userJoinedChatFmt)
          case _: ChatMessage => write(chatMessageFmt)
          case _: ChatInfo => write(chatInfoFmt)
          case _: AvailableGames => write(availableGamesFmt)
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
          case JsSuccess(body, _) => JsSuccess(PackagedMessage(msgType, body))
          case error: JsError => error

      }

    }

  implicit def toPackagedMessage[T <: CitadelMessage](msg: T): PackagedMessage[T] =
      PackagedMessage(msg.requestTitle, msg)

  implicit def parseJsError(jsError: JsError): Seq[String] =
    jsError.errors.flatMap(_._2).flatMap(_.messages)

}