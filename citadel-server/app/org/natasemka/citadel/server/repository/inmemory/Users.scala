package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.User
import org.natasemka.citadel.server.repository.api.UserRepo

import scala.collection.mutable

object Users extends RepoWithId[String, User] with UserRepo {
  private val passwords = mutable.Map[String, String]()

  private def internalError(userId: String) =
    s"Internal Server Error: user $userId was authenticated but does not exist in the database"

  override def signIn(login: String, password: String): Either[Exception, User] =
    passwords.get(login) match {
      case Some(exstPass) =>
        if (exstPass != password) Left(new RuntimeException("Invalid password"))
        else entities.get(login) match {
          case (Some(user)) => Right(user)
          case _ => Left(new RuntimeException(internalError(login)))
        }
      case _ => signUp(login, password)
    }

  override def signUp(login: String, password: String): Either[Exception, User] =
    entities.get(login) match {
      case Some(_) => Left(new RuntimeException(s"User with id $login already exists"))
      case None =>
        passwords.put(login, password)
        Right(User(login, None))
    }
}
