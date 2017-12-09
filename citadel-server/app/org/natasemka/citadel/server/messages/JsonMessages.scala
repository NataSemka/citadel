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
  val AuthenticateMsg: String = "Authenticate"
  val AuthenticatedMsg: String = "Authenticated"
  val notAuthenticatedMsg: String = "NotAuthenticated"

  val LobbyInfoMsg: String = "LobbyInfo"
  val UserJoinedLobbyMsg: String = "UserJoinedLobby";
  val UserLeftLobby: String = "UserLeftLobby"

  val JoinLobbyMsg: String = "JoinLobby"
  val CreateGameMsg: String = "CreateGame"
  val JoinGameMsg: String = "JoinGame"

  val ChatMsg: String = "Chat"

  val InvalidMessage: String = """{"type":"InvalidMessage","body":{"descr":"Not a proper JSON message"}}"""
  def rejected(reason: String) = s"""{"type":"Rejected", "reason":"$reason"}"""

  implicit val authenticateReads = Json.reads[Authenticate]
  implicit val authenticateFormat = Json.format[Authenticate]
  implicit val notAuthenticatedWrites = Json.writes[NotAuthenticated]
  implicit val notAuthenticatedFormat = Json.format[NotAuthenticated]
}

