package org.natasemka.citadel.server.repository.api

trait Repository[K,V] {
  def create(entity: V): V
  def get(id: K): Option[V]
  def update(entity: V): Either[Exception, Boolean]
  def delete(id: K): Either[Exception, Boolean]
}
