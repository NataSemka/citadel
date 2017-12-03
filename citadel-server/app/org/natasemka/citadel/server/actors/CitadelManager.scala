package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.util.Timeout
import org.natasemka.citadel.server.messages._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.{JsDefined, JsString, JsValue}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CitadelManager @Inject()()
                     (implicit val system: ActorSystem,
                      implicit val ec: ExecutionContext)
  extends Actor with InjectedActorSupport with ActorLogging
{
  implicit val timeout: Timeout = Timeout(2.seconds)
  val logger = play.api.Logger(getClass)

  // the actor that manages a registry of actors and replicates
  // the entries to peer actors among all cluster nodes tagged with a specific role.
  val mediator: ActorRef = DistributedPubSub(system).mediator

  override def receive: Receive = {
    case msg: CitadelMessage =>
      msg match {
        case authInfo: Authenticate => sender ! Authenticated
        case unrecognized => sender ! """{"type":"UnrecognizedServerMessage"}"""
      }
  }

  private def authenticate(msgBody: JsValue): Either[Exception, JsValue] = {
    logger.debug(s"authentication request: $msgBody")

    val loginLookup = msgBody \ "login"
    val passLookup = msgBody \ "password"
    loginLookup match {
      case (JsDefined(JsString(login))) =>
        val name = s"socketActor-$login"
        log.info(s"Creating client scocket '$name'")
//        val child: ActorRef = injectedChild(socketFactory("lobby", self, self), name)
//        val future = (child ? ClientSocket.JoinLobby(name, "lobby")).mapTo[Flow[JsValue, JsValue, _]]
//        pipe(future) to sender
      case _ => illegal(s"Unrecognized authentication message format: $msgBody")
    }
    null
  }

  private def illegal(errorMsg: String): Either[Exception, JsValue] =
    Left(new IllegalArgumentException(errorMsg))
}