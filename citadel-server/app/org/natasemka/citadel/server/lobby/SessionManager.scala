package org.natasemka.citadel.server.lobby

import java.util.UUID
import javax.inject.{Inject, Named}

import akka.actor._
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl._
import akka.util.Timeout
import org.natasemka.citadel.server.actors.ClientSocket
import org.natasemka.citadel.server.messages.JsonMessage
import org.natasemka.citadel.server.messages.JsonMessages._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.{JsDefined, JsString, JsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SessionManager @Inject()
                            (socketFactory: ClientSocket.Factory,
                            @Named("citadelManager") citadelManager: ActorRef)
                            (implicit val ec: ExecutionContext)
  extends Actor with InjectedActorSupport with ActorLogging
{
  implicit val timeout: Timeout = Timeout(2.seconds)
  val logger = play.api.Logger(getClass)

  override def receive: Receive = {
    case _: String =>
      println("SessionManager received Create message")
      val id = UUID.randomUUID().toString
      createSocket(id)
    case msg => JsonMessage.processMsg(msg, (msgType, msgBody) =>
        msgType match {
          case AuthenticateMsg => authenticate(msgBody)
          case _ => illegal(s"Unrecognized message type: $msgType")
        }
      )
  }

  private def authenticate(msgBody: JsValue): Either[Exception, JsValue] = {
    logger.debug(s"authentication request: $msgBody")
    println(s"authentication request: $msgBody")

    val loginLookup = msgBody \ "login"
    loginLookup match {
      case (JsDefined(JsString(login))) => createSocket(login)
      case _ => illegal(s"Unrecognized authentication message format: $msgBody")
    }

    null
  }

  private def createSocket(id: String) = {
    val name = s"socketActor-$id"
    log.info(s"Creating client scocket '$name'")
    println(s"Creating client scocket '$name'")
    val child: ActorRef = injectedChild(socketFactory(citadelManager), name)
    val future = (child ? ClientSocket.JoinLobby(name, "lobby")).mapTo[Flow[JsValue, JsValue, _]]
    pipe(future) to sender
  }

  private def illegal(errorMsg: String): Either[Exception, JsValue] =
    Left(new IllegalArgumentException(errorMsg))
}

object SessionManager {
  case class Create()
}