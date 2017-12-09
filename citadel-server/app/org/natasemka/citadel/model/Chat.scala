package org.natasemka.citadel.model

case class Chat(
               id: Int,
               // lobby / private / game
               `type`: String,
               name: String
               )
