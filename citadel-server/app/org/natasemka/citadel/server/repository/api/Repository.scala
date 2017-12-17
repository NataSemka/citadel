package org.natasemka.citadel.server.repository.api

trait Repository[K,V] {
  def create(entity: V): V
  def create(entities: Seq[V]): Seq[V]
  def get(id: K): Option[V]
  def get(ids: Seq[K]): Seq[V]
  def update(entity: V): Either[Exception, Boolean]
  def update(entities: Seq[V]): Seq[Either[Exception, Boolean]]
  def delete(id: K): Either[Exception, Boolean]
  def delete(ids: Seq[K]): Seq[Either[Exception, Boolean]]
}
