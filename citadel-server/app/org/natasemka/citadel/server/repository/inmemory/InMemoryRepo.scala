package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.server.repository.api.Repository

import scala.collection.mutable

trait InMemoryRepo[K,V] extends Repository[K,V] {
  protected val entities: mutable.Map[K,V] = mutable.Map[K,V]()

  override def create(entities: Seq[V]): Seq[V] =
    entities.map(create)

  override def get(id: K): Option[V] = entities.get(id)

  override def get(ids: Seq[K]): Seq[V] =
    ids.map(get).filter(_.isDefined).map(_.get)

  override def update(entities: Seq[V]): Seq[Boolean] =
    entities.map(update)

  override def delete(id: K): Boolean = {
    entities.remove(id)
    true
  }

  override def delete(ids: Seq[K]): Seq[Boolean] =
    ids.map(delete)
}
