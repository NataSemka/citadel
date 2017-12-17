package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.{Chat}
import org.natasemka.citadel.server.repository.api.ChatRepo

import scala.collection.mutable

object Chats extends RepoWithOptId[String, Chat] with ChatRepo {
  private val byUser = mutable.Map[String, mutable.Set[Chat]]()
  private val users = mutable.Map[String, mutable.Set[String]]()

  override protected def toChatId(counter: Long, chat: Chat): String =
    s"${chat.`type`.toString}-$counter"

  private def chatDoesNotExist(chatId: String) =
    new RuntimeException(s"Chat $chatId does not exist")

  private def updateChatUsers(userId: String, chatId: String)
                    (chatHandler: (String, String, Chat) => Unit) =
    entities.get(chatId) match {
      case Some(chat) =>
        chatHandler.apply(userId, chatId, chat)
        Right(chat)
      case _ => Left(chatDoesNotExist(chatId))
    }

  override def subscribe(userId: String, chatId: String): Either[Exception, Chat] =
    updateChatUsers(userId, chatId)((userId, chatId, chat) => {
      byUser.get(userId) match {
        case Some(chats) => chats + chat
        case _ => byUser.put(userId, mutable.Set[Chat](chat))
      }
      users.get(chatId) match {
        case Some(chatUsers) => chatUsers + userId
        case _ => users.put(chatId, mutable.Set[String](userId))
      }
    })

  override def unsubscribe(userId: String, chatId: String): Either[Exception, Chat] =
    updateChatUsers(userId, chatId)((userId, chatId, chat) => {
      byUser.get(userId).foreach(_ - chat)
      users.get(chatId).foreach(users => {
        users - userId
        if (users.isEmpty) delete(chatId)
      })
    })

  private def setFromMapToSeq[K,V](map: mutable.Map[K,mutable.Set[V]], key: K): Either[Exception, Seq[V]] =
    map.get(key) match {
      case Some(set) => Right(set.toSeq)
      case _ => Right(Seq.empty)
    }

  override def usersIn(chatId: String): Either[Exception, Seq[String]] =
    setFromMapToSeq(users, chatId)

  override def ofUser(userId: String): Either[Exception, Seq[Chat]] =
    setFromMapToSeq(byUser, userId)

}
