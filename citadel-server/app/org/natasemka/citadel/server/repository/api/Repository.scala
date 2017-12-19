package org.natasemka.citadel.server.repository.api

trait Repository[K,V] {
  def create(entity: V): V
  def create(entities: Seq[V]): Seq[V]
  def get(id: K): Option[V]
  def get(ids: Seq[K]): Seq[V]
  def update(entity: V): Boolean
  def update(entities: Seq[V]): Seq[Boolean]
  def delete(id: K): Boolean
  def delete(ids: Seq[K]): Seq[Boolean]
}
