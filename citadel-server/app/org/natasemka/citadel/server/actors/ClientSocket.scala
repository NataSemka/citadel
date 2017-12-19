package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.event.LoggingReceive
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import org.natasemka.citadel.server.messages.CitadelMessages._
import org.natasemka.citadel.server.messages._
import play.api.libs.json._

import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

class ClientSocket @Inject()(@Assisted out: ActorRef, @Assisted manager: ActorRef)
                   extends Actor with ActorLogging
{
  val logger = play.api.Logger(getClass)
  implicit val timeout: Timeout = Timeout(50.millis)

  var isAuthenticated: Boolean = false
  var userId: Option[String] = None

  override def receive: Receive = LoggingReceive {
    case msg: String => handleUserRequest(msg)
    case msg: CitadelMessage => handleServerEvent(msg)
    case msg => handleInvalidRequest(msg)
  }

  def handleUserRequest(msg: String): Unit = {
    Try(Json.parse(msg)) match {
      case Success(json) => handleUserRequest(json)
      case Failure(e) => reject(Rejected.undefined(e.getMessage))
    }
  }

  def handleUserRequest(msg: JsValue): Unit = {
    val logMsg = s"ClientSocket received a JSON msg: $msg"
    logger.debug(logMsg)

    val reasons = mutable.Seq()
    val request = msg \ "type" match {
      case JsDefined(JsString(title)) => title
      case JsDefined(notString) =>
        reasons + s"Invalid type format: expected a string but found $notString"
        "Undefined"
      case _ => "Undefined"
    }

    Json.fromJson[PackagedMessage[CitadelMessage]](msg) match {
      case JsSuccess(PackagedMessage(_, body), _) => handleUserRequest(body)
      case error: JsError =>
        val errors = reasons ++ error
        reject(Rejected.invalidJson(request, errors))
    }
  }

  def handleUserRequest(msg: CitadelMessage): Unit =
    (userId, msg) match {
      case (Some(id), msgWithId: UserIdMessage) if id != msgWithId.userId =>
        reject(Rejected.notAuthorized(msg))
      case (None, _: Credentials) =>
        manager ! msg
      case (None, _) =>
        reject(Rejected.notAuthenticated(msg))
      case _ =>
        manager ! msg
    }

  def handleServerEvent(msg: CitadelMessage): Unit = {
    msg match {
      case auth: Authenticated =>
        isAuthenticated = true
        userId = Some(auth.user.id)
      case _ =>
    }
    out ! msg.packageJson
  }

  def handleInvalidRequest(msg: Any): Unit =
    reject(Rejected.invalidRequest(msg))

  def reject(msg: Rejected): Unit =
    out ! msg.packageJson
}

object ClientSocket {
  trait Factory {
    def apply(manager: ActorRef): Actor
  }

  def props(out: ActorRef, manager: ActorRef) =
    Props(new ClientSocket(out, manager))
}