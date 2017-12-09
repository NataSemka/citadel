package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import org.natasemka.citadel.server.messages.CitadelMessages._
import org.natasemka.citadel.server.messages._
import play.api.libs.json._

import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class ClientSocket @Inject()(@Assisted out: ActorRef, @Assisted manager: ActorRef)
                   extends Actor with ActorLogging
{
  val logger = play.api.Logger(getClass)
  implicit val timeout: Timeout = Timeout(50.millis)

  override def receive: Receive = LoggingReceive {
    case msg: String => handleUserRequest(msg)
    case msg: CitadelMessage => handleServerEvent(msg)
    case msg => handleInvalidRequest(msg)
  }

  def handleUserRequest(msg: String): Unit = {
    Try(Json.parse(msg)) match {
      case Success(json) => handleUserRequest(json)
      case Failure(e) => rejectInvalidJson("Undefined", e.getMessage)
    }
  }

  def handleUserRequest(msg: JsValue): Unit = {
    val logMsg = s"ClientSocket received a JSON msg: $msg"
    logger.debug(logMsg)

    val titleLookup = msg \ "type" match {
      case JsDefined(JsString(title)) => title
      case JsDefined(notString) => notString.toString
      case _ => "Undefined"
    }

    Json.fromJson[PackagedMessage[CitadelMessage]](msg) match {
      case JsSuccess(PackagedMessage(title, body), _) => handleUserRequest(title, body)
      case JsError(e) =>
        val errs = e.flatMap(_._2).map(_.message)
        val errmsg =
          if (errs.lengthCompare(1) == 0) errs.head
          else errs.map(err => s"[$err]").mkString("; ")
        rejectInvalidJson(titleLookup, errmsg)
    }
  }

  def handleUserRequest(title: String, msg: CitadelMessage): Unit = msg match {
    case cm => manager ! cm
    // can include extra processing based on title / msg type
  }

  def handleServerEvent(msg: CitadelMessage): Unit =
    out ! msg.packageJson

  def handleInvalidRequest(msg: Any): Unit =
    rejectInvalidJson("Undefined", s"Invalid message format: $msg")

  def reject(title: String, request: String, reason: String): Unit =
    out ! Rejected(title, request, reason).packageJson

  def rejectInvalidJson(request: String, reason: String): Unit =
    reject("Invalid JSON", request, reason)

  def rejectInvalidJson(request: String): Unit =
    reject("Invalid JSON", request, "Received a message that is not a valid JSON")
}

object ClientSocket {
  trait Factory {
    def apply(manager: ActorRef): Actor
  }

  def props(out: ActorRef, manager: ActorRef) =
    Props(new ClientSocket(out, manager))
}