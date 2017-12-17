package org.natasemka.citadel.server.repository.inmemory

import org.natasemka.citadel.model.WithId

case class RepoWithId[K,V <: WithId[K]]() extends InMemoryRepo[K,V] {
  override def create(entity: V): V = {
    entities.put(entity.id, entity)
    entity
  }

  override def update(entity: V): Either[Exception, Boolean] = {
    entities.put(entity.id, entity)
    Right(true)
  }

}