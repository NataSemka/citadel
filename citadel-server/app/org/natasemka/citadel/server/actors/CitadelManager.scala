package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.actor._
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator._
import akka.util.Timeout
import org.natasemka.citadel.model._
import org.natasemka.citadel.server.messages.JsonMessages._
import org.natasemka.citadel.server.messages._
import org.natasemka.citadel.server.repository.api.Repositories
import play.api.libs.concurrent.InjectedActorSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class CitadelManager @Inject()()
                     (implicit val system: ActorSystem,
                      implicit val ec: ExecutionContext,
                      implicit val repositories: Repositories)
  extends Actor with InjectedActorSupport with ActorLogging
{
  implicit val timeout: Timeout = Timeout(2.seconds)
  val logger = play.api.Logger(getClass)

  // the actor that manages a registry of actors and replicates
  // the entries to peer actors among all cluster nodes tagged with a specific role.
  private val mediator: ActorRef = DistributedPubSub(system).mediator

  private val users = repositories.users
  private val games = repositories.games
  private val chats = repositories.chats

  //var topicCounter = 2
  var lobbyId = "0"
  //def lobbyTopic = "Lobby"

  def unrecognizedCommand(cmd: Any) =
    logger.error(s"Received an unrecognized server command: $cmd")

  override def receive: Receive = {
    case msg: CitadelMessage =>
      msg match {
        case credentials: Credentials => signIn(credentials)
        case chatMsg: ChatMessage => chat(chatMsg)
        case createGameCmd: CreateGame => createGame(createGameCmd)
        case _ => unrecognizedCommand(msg)
      }
    case _ => unrecognizedCommand(_)
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
    users.signIn(userId, password) match {
      case Right(_) => _
      case Left(e) => Left(Rejected("Not Authenticated", SignInMsg, e.getMessage))
    }
  }

  def loadUser(user: User): Unit = {
    games.ofUser(user.id) match {
      case Some(session) => sender ! session
      case None => joinLobby(user)
    }
  }

//  def joinLobby(user: User): Unit = {
//    val topic = lobbyTopic
//    mediator ! Subscribe(lobbyId, sender)
//    mediator ! Publish(topic, UserJoinedLobby(user))
//    sender ! LobbyInfo(usersInLobby, Seq.empty)
//  }
  def joinChat(user)

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