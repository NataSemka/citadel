package org.natasemka.citadel.server.messages

import play.api.libs.json.{JsDefined, JsString, JsValue, Json}

object JsonMessage {
  val MsgType: String = "type"
  val Headers: String = "headers"
  val Body: String = "body"

  def processMsg(msg: Any, handle: (String, JsValue) => Either[Exception, JsValue]): Either[Exception, JsValue] =
    msg match {
      case json: JsValue =>
        val msgTypeLookup = json \ MsgType
        val msgBodyLookup = json \ Body
        (msgTypeLookup, msgBodyLookup) match {
          case (JsDefined(JsString(msgType)), JsDefined(msgBody)) => handle(msgType, msgBody)
          case _ => invalidMessage(msg)
        }
      case _ => invalidMessage(msg)
    }

  def invalidMessage(msg: Any): Left[Exception, JsValue] =
    Left(illegal(s"Invalid message format: $msg"))

  def illegal(errorMsg: String): IllegalArgumentException = new IllegalArgumentException(errorMsg)

}

object JsonMessages {
  val ServerErrorMsg: String = "ServerError"
  val RejectedMsg: String = "Rejected"

  val SignInMsg: String = "SignIn"
  //val AuthenticateMsg: String = "Authenticate"
  val AuthenticatedMsg: String = "Authenticated"
  val NotAuthenticatedMsg: String = "NotAuthenticated"

  val JoinLobbyMsg: String = "JoinLobby"
  val LobbyInfoMsg: String = "LobbyInfo"
  val UserLeftLobby: String = "UserLeftLobby"

  val ChatMsg: String = "Chat"
  val ChatInfoMsg: String = "ChatInfo"
  val UserJoinedChatMsg: String = "UserJoinedChat"
  val UserLeftChatMsg: String = "UserLeftChat"
  val AvailableChatsMsg: String = "AvailableChats"
  val NewChatAvailableMsg: String = "NewChatAvailable"
  val ChatNoLongerAvailableMsg: String = "ChatNoLongerAvailable"

  val CreateGameMsg: String = "CreateGame"
  val JoinGameMsg: String = "JoinGame"
  val LeaveGameMsg: String = "LeaveGame"
  val UserJoinedGameMsg: String = "UserJoinedGame"
  val UserLeftGameMsg: String = "UserLeftGame"
  val AvailableGamesMsg: String = "AvailableGames"

  implicit val notAuthenticatedWrites = Json.writes[NotAuthenticated]
  implicit val notAuthenticatedFormat = Json.format[NotAuthenticated]
}