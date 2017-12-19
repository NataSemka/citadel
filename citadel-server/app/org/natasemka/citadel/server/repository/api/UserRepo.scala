package org.natasemka.citadel.server.repository.api

import org.natasemka.citadel.model.User

trait UserRepo extends Repository[String, User] {
  def signIn(login: String, password: String): Either[Exception, User]
  def signUp(login: String, password: String): Either[Exception, User]
}
