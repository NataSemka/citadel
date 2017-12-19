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
import scala.util.{Failure, Success, Try}

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

  private val lobbyId = "Lobby"
  private val lobby: Chat = Chat(Some(lobbyId), Lobby, lobbyId)

  private def unrecognizedCommand(cmd: Any): Unit =
    logger.error(s"Received an unrecognized server command: $cmd")

  override def preStart(): Unit = {
    super.preStart()
    chats.create(lobby)
  }

  override def receive: Receive = {
    case msg: CitadelMessage =>
      Try(handleRequest(msg)) match {
        case Success(_) => // TODO emit success / seq of replies ?
        case Failure(e) => Rejected.internalServerError(msg, e.getMessage)
      }
    case _ => unrecognizedCommand(_)
  }

  def handleRequest(msg: CitadelMessage): Unit =
    msg match {
      case credentials: Credentials => signIn(credentials)
      case chatMsg: ChatMessage => chat(chatMsg)
      case createGameCmd: CreateGame => createGame(createGameCmd)
      case _ => unrecognizedCommand(msg)
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
      case Right(user) => Right(user)
      case Left(e) => Left(Rejected.notAuthenticated(credentials, e.getMessage))
    }
  }

  def loadUser(user: User): Unit = {
    val privateChat = chats.create(Chat(Some(user.id), Private, user.id))
    joinChat(user, privateChat)
    games.ofUser(user.id) match {
      case Some(session) => sender ! session
      case _ => joinLobby(user)
    }
  }

  def joinLobby(user: User): Unit = {
    joinChat(user, lobby)
    sender ! AvailableGames(games.pending)
  }

  def joinChat(user: User, chat: Chat): Unit =
    chat.id match {
      case Some(chatId) =>
        chats.subscribe(user.id, chatId)
        mediator ! Subscribe(chatId, sender)
        chat.`type` match {
          case Private =>
          case _ =>
            sender ! ChatInfo(chat, chat)
            mediator ! Publish(chatId, UserJoinedChat(user, chat.id.get))
        }
      case None => throw new RuntimeException(s"Encountered a chat without id: $chat")
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
    val chatId = chatMsg.chatId
    chats.get(chatId) match {
      case Some(_) =>
        if (chats.usersIn(chatId).contains(chatMsg.userId)) {
          val withTimestamp = chatMsg.copy(timestamp = Some(System.currentTimeMillis()))
          mediator ! Publish(chatId, withTimestamp)
        } else
          sender ! Rejected("No Such Chat", ChatMsg, s"Chat $chatId does not exist")
      case _ => sender ! Rejected("No Such Chat", ChatMsg, s"Chat $chatId does not exist")
    }
  }

  implicit def usersInChat(chat: Chat): Seq[User] =
    users.get(chats.usersIn(chat.id.get))

}