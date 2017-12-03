package org.natasemka.citadel.model

case class Round (
    players: Seq[Player],
    currentPlayer: Player,
    crownedPlayer: Player,
    characterDeck: CharacterDeck,
    quarterDeck: QuarterDeck
    )