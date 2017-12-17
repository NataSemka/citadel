package org.natasemka.citadel.server.repository.api

trait Repositories {
  def users: UserRepo
  def games: GameRepo
  def chats: ChatRepo
}