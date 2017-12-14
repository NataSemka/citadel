package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.server.repository.api.Repository

import scala.collection.mutable

trait InMemoryRepo[K,V] extends Repository[K,V] {
  protected val values: mutable.Map[K,V] = mutable.Map[K,V]()

  override def get(id: K): Option[V] = values.get(id)

  override def delete(id: K): Either[Exception, Boolean] = {
    values.remove(id)
    Right(true)
  }
}
