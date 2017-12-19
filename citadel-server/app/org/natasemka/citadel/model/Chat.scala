package org.natasemka.citadel.model

case class Chat(id: Option[String], `type`: ChatType, name: String) extends WithOptId[String, Chat] {
  override def withId(id: String): Chat = copy(id = Some(id))
}

sealed trait ChatType

object ChatType {
  def of(id: String): ChatType = id match {
    case "lobby" => Lobby
    case "group" => Group
    case "private" => Private
  }
}

case object Lobby extends ChatType {
  override def toString = "lobby"
}
case object Group extends ChatType {
  override def toString = "group"
}
case object Private extends ChatType {
  override def toString = "private"
}