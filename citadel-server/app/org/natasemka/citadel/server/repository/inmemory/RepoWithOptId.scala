package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.WithOptId

case class RepoWithOptId[K, V <: WithOptId[K,V]]()(implicit val idGen: KeyGen[K]) extends InMemoryRepo[K, V] {
  override def create(entity: V): V = {
    val id = idGen.next
    val s = entity.withId(id)
    values.put(id, s)
    s
  }

  override def update(entity: V): Either[Exception, Boolean] = {
    entity.id match {
      case Some(id) => values.put(id, entity)
      case None => create(entity)
    }
    Right(true)
  }
}

trait KeyGen[Key] {
  def next: Key
}

