package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.util.Timeout
import org.natasemka.citadel.model.{GameSession, User}
import org.natasemka.citadel.server.messages._
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json.JsValue

import scala.collection.mutable
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

  var topicCounter = 2;
  def lobbyTopic = "Lobby"

  //private var userCounter: Int = 0
  private var userById: mutable.Map[String, User] = mutable.Map()

  private var sessionCounter: Int = 0
  private val gameById = mutable.Map[Int, GameSession]()
  private val gameByUser = mutable.Map[String, Int]()

  private val unrecognizedMsg = """{"type":"UnrecognizedServerMessage"}"""

  override def receive: Receive = {
    case msg: CitadelMessage =>
      msg match {
        case credentials: Credentials => signIn(credentials)
        case chatMsg: ChatMessage => chat(chatMsg)
        case _ => sender ! unrecognizedMsg
      }
    case _ => sender ! unrecognizedMsg
  }

  def signIn(credentials: Credentials): Unit = {
    logger.debug(s"Sign in request from ${credentials.userId}")
    getUser(credentials) match {
      case Right(user) => loadUser(user)
      case Left(response) => sender ! response
    }
  }

  def getUser(credentials: Credentials): Either[CitadelMessage, User] = {
    // TODO: player does not exist / invalid password
    val (userId, password) = (credentials.userId, credentials.password)
    userById.get(userId) match {
      case Some(user) =>
        if (user.password == password) Right(user)
        else Left(NotAuthenticated("Wrong password"))
      case None =>
        val user = User(userId, password, None)
        userById += userId -> user
        Right(user)
    }
  }

  def loadUser(user: User): Unit = {
    isInGame(user) match {
      case Some(sessionId) => joinGame(sessionId, user)
      case None => joinLobby(user)
    }
  }

  def isInGame(user: User): Option[Int] = isInGame(user.id)

  def isInGame(userId: String): Option[Int] = {
    gameByUser.get(userId)
  }

  def joinLobby(user: User): Unit = {
    val topic = lobbyTopic
    mediator ! Subscribe(topic, sender)
    mediator ! Publish(topic, UserJoinedLobby(user))
  }

  def joinGame(sessionId: Int, user: User): Unit = ???

  def chat(chatMsg: ChatMessage) = {
    val withTimestamp = chatMsg.copy(timestamp = System.currentTimeMillis())
    mediator ! Publish(chatMsg.chatId.toString, withTimestamp)
  }



  private def illegal(errorMsg: String): Either[Exception, JsValue] =
    Left(new IllegalArgumentException(errorMsg))
}