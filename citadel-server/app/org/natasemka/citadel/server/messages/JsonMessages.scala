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
  val AuthenticateMsg: String = "Authenticate"
  val SignInMsg: String = "SignIn"
  val CreateGameMsg: String = "CreateGame"
  val JoinLobbyMsg: String = "JoinLobby"
  val JoinGameMsg: String = "JoinGame"

  val InvalidMessage: String = """{"type":"InvalidMessage","body":{"descr":"Not a proper JSON message"}}"""

  implicit val authenticateReads = Json.reads[Authenticate]
  implicit val authenticateFormat = Json.format[Authenticate]
  implicit val notAuthenticatedWrites = Json.writes[NotAuthenticated]
  implicit val notAuthenticatedFormat = Json.format[NotAuthenticated]
}

