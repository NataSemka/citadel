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
          case JoinLobbyMsg => joinLobby(msgBody)
          case _ => out ! s"Unrecognized message type: $msgType"
        }
      case _ => out ! s"""{"type": "Rejected", "reason":"invalid message type: $msg"}"""
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
        //out ! "Not Authenticated"
    }
  }

  def joinLobby(msg: JsValue): Either[Exception, String] = {
    val lobbyIdLookup = msg \ "lobbyId"
    lobbyIdLookup match {
      case JsDefined(JsString(lobbyId)) =>
        //mediator ! Subscribe(lobbyId, self)
        this.lobbyId = Some(lobbyId)
        Right(lobbyId)
      case _ => Left(illegal(s"Unexpected lobby ID value: $lobbyIdLookup"))
    }
  }

  def joinGame(msg: JsValue): Either[_, String] = {
    val sessionIdLookup = msg \ "sessionId"
    sessionIdLookup match {
      case JsDefined(JsString(sessionId)) =>
        //mediator ! Subscribe(sessionId, self)
        this.sessionId = Some(sessionId)
        Right(sessionId)
      case _ => Left(illegal(s"Unexpected session ID value: $sessionIdLookup"))
    }
  }

  def handleServerMessage(msg: CitadelMessage): Unit = {
    msg match {
      case _: Authenticated => out ! """{"type":"Authenticated"}"""
      case _ => out ! """{"type":"UnrecognizedServerMessage"}"""
    }
  }

  def handleInvalidMessage(msg: Any): Unit = {
    val logMsg = s"Received invalid message: $msg"
    logger.debug(logMsg)
    out ! InvalidMessage
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
