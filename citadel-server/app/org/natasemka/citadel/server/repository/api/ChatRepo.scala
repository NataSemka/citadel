package org.natasemka.citadel.server.repository.api

import org.natasemka.citadel.model.Chat

trait ChatRepo extends Repository[Long,Chat] {
  def subscribe(userId: String, chatId: String): Either[Exception, Chat]
  def unsubscribe(userId: String, chatId: String): Either[Exception, Chat]
  def usersIn(chatId: String): Either[Exception, Seq[String]]
  def ofUser(userId: String): Either[Exception, Seq[String]]
}
