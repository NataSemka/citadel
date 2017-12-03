package org.natasemka.citadel.model

case class GameSession (
    id: Long,
    playerCapacity: Int,
    players: Seq[Player],
    round: Round,
    rules: Rules
    )