package org.natasemka.citadel.model

trait WithId[K] {
  def id: K
}

trait WithOptId[K,V] {
  def id: Option[K]
  def withId(id: K): V
}