package org.natasemka.citadel.server.controllers

import javax.inject.{Inject, Named, Singleton}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import org.natasemka.citadel.server.actors.{ClientSocket, SessionManager}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class CitadelController @Inject()
    (
      @Named("citadelManager") citadelManager: ActorRef,
      cc: ControllerComponents
    )
    (implicit val system: ActorSystem, val mat: Materializer, val ec: ExecutionContext)
    extends AbstractController(cc) with SameOriginCheck
{
  val logger = play.api.Logger(getClass)

   /**
   * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
   * Future[Flow], which is required internally.
   *
   * @return a fully realized websocket.
   */
  def ws: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      //wsFutureFlow(rh).map { flow =>
      //Right(flow)
      Future.successful {
        Right(ActorFlow.actorRef(out => ClientSocket.props(out, citadelManager)))
      }.recover {
        case e: Exception =>
          logger.error("Cannot create websocket", e)
          val jsError = Json.obj("error" -> "Cannot create websocket")
          val result = InternalServerError(jsError)
          Left(result)
      }
    case rejected =>
      logger.error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }
  
//  def authenticate: Action[AnyContent] = Action.async {
//    println("authenticating")
//    Future(Ok())
//  }
//    Action.async { request: Request[AnyContent] =>
//      val json = request.body.asJson.get
//      val credentials = Json.fromJson[Credentials](json).get
//      val futureResponse = sessionManager ? SignIn(credentials)
//      futureResponse.recover {
//        case e: Exception =>
//          InternalServerError(Json.obj(
//            "error" -> e.toString,
//            "description" -> e.getMessage
//          ))
//      }
//      futureResponse.map(response => {
//        val r = response.asInstanceOf[String]
//        Ok(r)
//      })
//    }

  /**
   * Creates a Future containing a Flow of JsValue in and out.
   */
  private def wsFutureFlow(request: RequestHeader): Future[Flow[JsValue, JsValue, NotUsed]] = {
    // Use guice assisted injection to instantiate and configure the child actor.
    implicit val timeout: Timeout = Timeout(1.second) // the first run in dev can take a while :-(
    val future: Future[Any] = citadelManager ? SessionManager.Create
    val futureFlow: Future[Flow[JsValue, JsValue, NotUsed]] = future.mapTo[Flow[JsValue, JsValue, NotUsed]]
    futureFlow
  }
  
}


trait SameOriginCheck {

  def logger: Logger

  /**
   * Checks that the WebSocket comes from the same origin.  This is necessary to protect
   * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
   *
   * See https://tools.ietf.org/html/rfc6455#section-1.3 and
   * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
   */
  def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
   * Returns true if the value of the Origin header contains an acceptable value.
   *
   * This is probably better done through configuration same as the allowedhosts filter.
   */
  def originMatches(origin: String): Boolean = {
    origin.contains("localhost:9000") ||
      origin.contains("chrome-extension://pfdhoblngboilpfeibdedpjgfnlcodoo") ||
      origin.contains("127.0.0.1:9000")
  }

}

case class Credentials(playerId: String, password: String)