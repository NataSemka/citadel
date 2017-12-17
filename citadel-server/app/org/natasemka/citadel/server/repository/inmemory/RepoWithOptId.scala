package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.WithOptId

trait RepoWithOptId[K, V <: WithOptId[K,V]] extends InMemoryRepo[K, V] {
  trait KeyGen[Key] {
    def next: Key
  }

  protected val idCounter: KeyGen[Long] = new KeyGen[Long] {
    var counter: Long = -1
    override def next: Long = {
      counter = counter + 1
      counter
    }
  }

  protected def toChatId(counter: Long, entity: V): K

  override def create(entity: V): V = {
    val id = toChatId(idCounter.next, entity)
    val s = entity.withId(id)
    entities.put(id, s)
    s
  }

  override def update(entity: V): Either[Exception, Boolean] = {
    entity.id match {
      case Some(id) => entities.put(id, entity)
      case None => create(entity)
    }
    Right(true)
  }
}


