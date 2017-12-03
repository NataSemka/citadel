package org.natasemka.citadel.server

import javax.inject.Singleton

import play.api.http.HttpErrorHandler
import play.api.libs.json._
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future

@Singleton
class ErrorHandler extends HttpErrorHandler {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] =
    if (statusCode == play.api.http.Status.NOT_FOUND)
      Future.successful(NotFound(Json.obj("error" -> "Not Found")))
    else
      //Future.successful(Status(statusCode))
      //super.onClientError(request, statusCode)
      Future.successful(Status(statusCode))

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] =
    Future.successful(InternalServerError(Json.obj(
      "error" -> exception.toString,
      "description" -> exception.getMessage
    )))
}
