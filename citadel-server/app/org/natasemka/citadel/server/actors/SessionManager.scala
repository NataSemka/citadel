package org.natasemka.citadel.server.actors

import java.util.UUID
import javax.inject.{Inject, Named}

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.pattern.{ask, pipe}
import akka.stream.scaladsl._
import akka.util.Timeout
import org.natasemka.citadel.server.Message
import org.natasemka.citadel.server.MessageTypes._
import org.natasemka.citadel.server.actors.SessionManager.Create
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
  implicit val timeout = Timeout(2.seconds)
  val logger = play.api.Logger(getClass)

  override def receive: Receive = {
    case _: Create =>
      println("SessionManager received Create message")
      val id = UUID.randomUUID().toString
      createSocket(id)
    case msg => Message.processMsg(msg, (msgType, msgBody) =>
        msgType match {
          case Authenticate => authenticate(msgBody)
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

    return null
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