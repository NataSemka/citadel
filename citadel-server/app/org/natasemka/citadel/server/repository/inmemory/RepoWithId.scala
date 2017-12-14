package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.WithId

case class RepoWithId[K,V <: WithId[K]]() extends InMemoryRepo[K,V] {
  override def create(entity: V): V = {
    values.put(entity.id, entity)
    entity
  }

  override def update(entity: V): Either[Exception, Boolean] = {
    values.put(entity.id, entity)
    Right(true)
  }
}