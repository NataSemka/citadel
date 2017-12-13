package org.natasemka.citadel.model

case class Chat(
               id: String,
               // lobby / private / game
               `type`: String,
               name: String
               )
