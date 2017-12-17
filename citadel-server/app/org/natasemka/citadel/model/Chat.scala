package org.natasemka.citadel.model

case class Chat(id: String, `type`: ChatType, name: String)

sealed trait ChatType
case object Lobby extends ChatType {
  override def toString = "lobby"
}
case object Group extends ChatType {
  override def toString = "group"
}
case object Private extends ChatType {
  override def toString = "private"
}