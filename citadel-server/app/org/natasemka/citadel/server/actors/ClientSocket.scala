package org.natasemka.citadel.server.actors

import javax.inject.Inject

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.implicitConversions

class ClientSocket @Inject()(@Assisted out: ActorRef, @Assisted manager: ActorRef)
                   extends Actor with ActorLogging
{
  val logger = play.api.Logger(getClass)

  implicit val timeout: Timeout = Timeout(50.millis)


//  val (hubSink, hubSource) = MergeHub.source[JsValue](perProducerBufferSize = 16)
//    .toMat(BroadcastHub.sink(bufferSize = 256))(Keep.both)
//    .run()

  private val jsonSink: Sink[JsValue, Future[Done]] = Sink.foreach { json =>
    // When the user types in a stock in the upper right corner, this is triggered,
    println(s"JSON Sink received a message")
  }

  //implicit val node = Cluster(system)


  private var lobbyId: Option[String] = None
  private var sessionId: Option[String] = None

  override def receive: Receive = {
    case msg: JsValue =>
      val logMsg = s"ClientSocket received a JSON msg: $msg"
      logger.info(logMsg)
      println(logMsg)
      out ! logMsg
    case msg =>
      val logMsg = s"ClientSocket received a msg: $msg"
      logger.info(logMsg)
      println(logMsg)
      out ! logMsg
//      sender ! websocketFlow
//    case msg: JsValue =>
//      val msgTypeLookup = msg \ MsgType
//      val msgBodyLookup = msg \ Body
//      (msgTypeLookup, msgBodyLookup)  match {
//        case (JsDefined(JsString(msgType)), JsDefined(msgBody)) =>
//          msgType match {
//            case JoinLobby => joinLobby(msgBody)
//            case JoinGame => joinGame(msgBody)
//            case _ => Left(illegal(s"Unrecognized message type: $msgType"))
//          }
//        case _ => Left(illegal(s"Invalid message format: $msg"))
//      }
  }

  /*
    * Generates a flow that can be used by the websocket.
    *
    * @return the flow of JSON
    */
//  private lazy val websocketFlow: Flow[JsValue, JsValue, NotUsed] = {
//    // Put the source and sink together to make a flow of hub source as output (aggregating all
//    // stocks as JSON to the browser) and the actor as the sink (receiving any JSON messages
//    // from the browser), using a coupled sink and source.
//    Flow.fromSinkAndSourceCoupled(jsonSink, hubSource).watchTermination() { (_, termination) =>
//      // When the flow shuts down, make sure this actor also stops.
//      termination.foreach(_ => context.stop(self))
//      NotUsed
//    }
//  }

//  def joinLobby(msg: JsValue): Either[Exception, String] = {
//    println("ClientSocket - JoinLobby processing...")
//    val lobbyIdLookup = msg \ "lobbyId"
//    lobbyIdLookup match {
//      case JsDefined(JsString(lobbyId)) =>
//        //mediator ! Subscribe(lobbyId, self)
//        this.lobbyId = Some(lobbyId)
//        Right(lobbyId)
//      case _ => Left(illegal(s"Unexpected lobby ID value: $lobbyIdLookup"))
//    }
//  }

  def joinGame(msg: JsValue): Either[_, String] = {
    val sessionIdLookup = msg \ "sessionId"
    sessionIdLookup match {
      case JsDefined(JsString(sessionId)) =>
        //mediator ! Subscribe(sessionId, self)
        this.sessionId = Some(sessionId)
        Right(sessionId)
      case _ => Left(illegal(s"Unexpected session ID value: $sessionIdLookup"))
    }
  }

  def illegal(errorMsg: String): IllegalArgumentException = new IllegalArgumentException(errorMsg)

}

object ClientSocket {
  trait Factory {
    def apply(manager: ActorRef): Actor
  }

  case class JoinLobby(id: String, topic: String)

  def props(out: ActorRef, manager: ActorRef) =
    Props(new ClientSocket(out, manager))
}
