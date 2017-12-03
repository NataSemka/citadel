package org.natasemka.citadel.model

case class Player(
      user: User,
      character: Character,
      coins: Int,
      quartersOnHand: Seq[Quarter],
      builtQuarters: Seq[Quarter]
    )