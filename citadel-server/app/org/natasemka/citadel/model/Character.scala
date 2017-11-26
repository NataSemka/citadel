package org.natasemka.citadel.model

case class Character(
    name: String, 
    description: String,
    color: Color,
    actions: Seq[Action]
    )