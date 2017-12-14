package org.natasemka.citadel.model

case class User(id: String, avatar: Option[String])
  extends WithId[String]