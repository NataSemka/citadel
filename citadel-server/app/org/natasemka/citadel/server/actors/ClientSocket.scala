package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import org.natasemka.citadel.server.messages._
import org.natasemka.citadel.server.messages.CitadelMessages._
import org.natasemka.citadel.server.messages.JsonMessage._
import org.natasemka.citadel.server.messages.JsonMessages._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class ClientSocket @Inject()(@Assisted out: ActorRef, @Assisted manager: ActorRef)
                   extends Actor with ActorLogging
{
  val logger = play.api.Logger(getClass)
  implicit val timeout: Timeout = Timeout(50.millis)
  //implicit val node = Cluster(system)


  private var lobbyId: Option[String] = None
  private var sessionId: Option[String] = None

  override def receive: Receive = LoggingReceive {
    case msg: String => handleUserMessage(msg)
    case msg: CitadelMessage => handleServerMessage(msg)
    case msg => handleInvalidMessage(msg)
  }

  def handleUserMessage(msg: String): Unit = {
    Try(Json.parse(msg)) match {
      case Success(json) => handleUserMessage(json)
      case Failure(e) => out ! e.getMessage
    }
  }

  def handleUserMessage(msg: JsValue): Unit = {
    val logMsg = s"ClientSocket received a JSON msg: $msg"
    logger.debug(logMsg)
    //out ! logMsg

    val msgTypeLookup = msg \ MsgType
    val msgBodyLookup = msg \ Body
    (msgTypeLookup, msgBodyLookup) match {
      case (JsDefined(JsString(msgType)), JsDefined(msgBody)) =>
        msgType match {
          case SignInMsg => signIn(msgBody)
          case JoinGameMsg => joinGame(msgBody)
          case ChatMsg => chat(msgBody)
          case _ => out ! rejected(s"Unrecognized message type: $msgType")
        }
      case _ => out ! rejected(s"Invalid message format: $msg")
    }
  }

  def signIn(msg: JsValue): Unit = {
    logger.debug("signing in")
    Json.fromJson[Credentials](msg) match {
      case JsSuccess(credentials, _) => manager ! credentials
      case e: JsError =>
        val notAuth: NotAuthenticated = NotAuthenticated(e.toString)
        val res = Json.stringify(Json.toJson(notAuth))
        out ! res
    }
  }

  def chat(msg: JsValue): Unit = {
    logger.debug("chatting")
    Json.fromJson[ChatMessage](msg) match {
      case JsSuccess(chatMsg, _) => chat(chatMsg)
      case e: JsError =>
        out ! rejected(e.toString)
    }
  }

  def chat(chatMsg: ChatMessage): Unit = {
    manager ! chatMsg
  }

  def joinGame(msg: JsValue): Either[_, String] = {
    val sessionIdLookup = msg \ "sessionId"
    sessionIdLookup match {
      case JsDefined(JsString(sessionId)) =>
        this.sessionId = Some(sessionId)
        Right(sessionId)
      case _ => Left(illegal(s"Unexpected session ID value: $sessionIdLookup"))
    }
  }

  def handleServerMessage(msg: CitadelMessage): Unit = {
    msg match {
      case msg: Authenticated =>
        val packaged = PackagedMessage("Authenticated", msg)
        val json = Json.toJson(packaged)
        out ! Json.stringify(json)
      case msg: UserJoinedLobby =>
        val packaged = PackagedMessage("UserJoinedLobby", msg)
        val json = Json.toJson(packaged)
        out ! Json.stringify(json)
      case msg: LobbyInfo =>
        val packaged = PackagedMessage("LobbyInfo", msg)
        val json = Json.toJson(packaged)
        out ! Json.stringify(json)
      case msg: ChatMessage => //emitPackaged("Chat", msg)
        val packaged = PackagedMessage("Chat", msg)
        val json = Json.toJson(packaged)
        out ! Json.stringify(json)
      case _ => logger.warn(s"Unrecognized server message: $msg")
    }
  }

  def emitPackaged[T <: CitadelMessage](title: String, msg: T): Unit = {
//    val packaged = PackagedMessage(title, msg)
//    val json = Json.toJson(packaged)
//    out ! Json.stringify(json)
  }

  def handleInvalidMessage(msg: Any): Unit = {
    val logMsg = s"Received invalid message: $msg"
    logger.debug(logMsg)
    out ! rejected(logMsg)
  }
}

object ClientSocket {
  trait Factory {
    def apply(manager: ActorRef): Actor
  }

  def props(out: ActorRef, manager: ActorRef) =
    Props(new ClientSocket(out, manager))

  def illegal(errorMsg: String): IllegalArgumentException = new IllegalArgumentException(errorMsg)
}
