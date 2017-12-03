package org.natasemka.citadel.model

case class User(
                 id: String,
                 password: String,
                 avatar: Option[String]
               )
