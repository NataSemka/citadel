package org.natasemka.citadel.server.actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import org.natasemka.citadel.model.{GameSession, Player}
import org.natasemka.citadel.server.actors.LobbyActor.SignIn
import org.natasemka.citadel.server.controllers.Credentials

import scala.collection.mutable

class LobbyActor extends Actor with ActorLogging {
  val sessionCounter = 0
  val playerPasswords: mutable.Map[String, String] = mutable.HashMap()
  val playersInLobby: mutable.Map[String, Player] = mutable.LinkedHashMap()
  val playersInGame: mutable.Map[String, GameSession] = mutable.HashMap()
  val sessions: mutable.Map[Long, GameSession] = mutable.LinkedHashMap()

  override def receive = LoggingReceive {
    case SignIn(credentials: Credentials) => signIn(credentials)
    case _ => println("unknown receive")
  }

  def signIn(credentials: Credentials): SignInResult = {
    val authResult = authenticate(credentials)
    val result = authResult match {
      case Authenticated() => loadPlayer(credentials.playerId)
      case _ => NotAuthenticated(authResult.msg)
    }
    result
  }

  def loadPlayer(playerId: String): SignInResult =
    if (playersInGame.keySet.contains(playerId))
      IsInGame(playersInGame(playerId))
    else if (playersInLobby.keySet.contains(playerId))
      IsInLobby()
    else
      JoinsLobby()


  def authenticate(credentials: Credentials): AuthenticationResult = {
    // commenting out until we have proper authentication
//    val playerId = credentials.login
//    val password = credentials.password
//    if (!playerPasswords.keySet.contains(playerId))
//      InvalidPlayerId()
//    else if (!(playerPasswords.get(playerId) == password))
//      InvalidPassword()
//    else
      Authenticated()
  }

}

trait WithMessage {
  val msg: String = "???"
}

trait AuthenticationResult extends WithMessage

case class InvalidPlayerId() extends AuthenticationResult {
  override val msg: String = "Invalid player ID"
}
case class InvalidPassword() extends AuthenticationResult {
  override val msg: String = "Invalid password"
}
case class Authenticated() extends AuthenticationResult {
  override val msg: String = "Authenticated"
}

trait SignInResult extends WithMessage
case class NotAuthenticated(override val msg: String) extends SignInResult
object NotAuthenticated {
  def apply(auth: AuthenticationResult): NotAuthenticated = NotAuthenticated(auth.msg)
}
case class JoinsLobby() extends SignInResult {
  override val msg: String = "Joined lobby"
}
case class IsInLobby() extends SignInResult {
  override val msg: String = "Is in Lobby"
}
case class IsInGame(session: GameSession) extends SignInResult {
  override val msg: String = "Is in game"
}

object LobbyActor {
  case class SignUp(credentials: Credentials)
  case class SignIn(credentials: Credentials)
  case class CreateGame(playerId: String)
  case class JoinGame(playerId: String, sessionId: Long)
}
