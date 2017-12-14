package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.util.Timeout
import org.natasemka.citadel.model._
import org.natasemka.citadel.server.messages.JsonMessages._
import org.natasemka.citadel.server.messages._
import play.api.libs.concurrent.InjectedActorSupport

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

  var topicCounter = 2
  def lobbyTopic = "Lobby"

  //private var userCounter: Int = 0
  private val userById: mutable.Map[String, User] = mutable.Map()
  private val passwordByUserId: mutable.Map[String, String] = mutable.Map()

  private var sessionCounter: Int = 0
  private val gameById = mutable.Map[Int, GameSession]()
  private val gameByUser = mutable.Map[String, Int]()

  private val unrecognizedMsg = """{"type":"UnrecognizedServerMessage"}"""

  override def receive: Receive = {
    case msg: CitadelMessage =>
      msg match {
        case credentials: Credentials => signIn(credentials)
        case chatMsg: ChatMessage => chat(chatMsg)
        case createGameCmd: CreateGame => createGame(createGameCmd)
        case _ => sender ! unrecognizedMsg
      }
    case _ => sender ! unrecognizedMsg
  }

  def signIn(credentials: Credentials): Unit = {
    logger.debug(s"Sign in request from ${credentials.userId}")
    getUser(credentials) match {
      case Right(user) =>
        sender ! Authenticated(user)
        loadUser(user)
      case Left(response) => sender ! response
    }
  }

  def getUser(credentials: Credentials): Either[CitadelMessage, User] = {
    val (userId, password) = (credentials.userId, credentials.password)

    def createUser: Either[CitadelMessage, User] = {
      passwordByUserId.put(userId, password)
      val user = User(userId, None)
      userById.put(userId, user)
      Right(user)
    }

    passwordByUserId.get(userId) match {
      case Some(expectedPass) =>
        if (expectedPass == password) userById.get(userId) match {
          case Some(user) => Right(user)
          case None => createUser
        }
        else Left(Rejected("Not Authenticated", SignInMsg, "Wrong password"))
      case None => createUser
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
    sender ! LobbyInfo(usersInLobby, Seq.empty)
  }

  def usersInLobby: Seq[User] = {
    val userIds = userById.keySet -- gameByUser.keySet
    userById.filterKeys(userIds.contains).values.toSeq
  }

  def createGame(cmd: CreateGame): GameSession = ???
//  {
//    val stadardRules = RuleSet("Standard Rules", Empty)
//    val rules = cmd.rules.getOrElse(stadardRules)
//    val gameName = cmd.name.getOrElse(s"${cmd.userId}'s ${rules.name} game")
//    val round = Round(Empty,)
//    GameSession(None, 0, Empty, round, rules)
//  }

  def joinGame(sessionId: Int, user: User): Unit = ???

  def chat(chatMsg: ChatMessage): Unit = {
    val withTimestamp = chatMsg.copy(timestamp = Some(System.currentTimeMillis()))
    mediator ! Publish(chatMsg.chatId.toString, withTimestamp)
  }

}