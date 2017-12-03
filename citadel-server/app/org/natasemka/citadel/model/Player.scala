package org.natasemka.citadel.model

case class Player(
      name: String,
      character: Character,
      coins: Int,
      quartersOnHand: Seq[Quarter],
      builtQuarters: Seq[Quarter]
    )