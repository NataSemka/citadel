package org.natasemka.citadel.server

import play.api.libs.json.{JsDefined, JsString, JsValue}


object MessageTypes {
  val Authenticate: String = "Authenticate"
  val JoinLobby: String = "JoinLobby"
  val JoinGame: String = "JoinGame"


}

object Message {
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