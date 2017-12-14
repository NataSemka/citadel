package org.natasemka.citadel.model

case class GameSession (
    id: Option[Long],
    playerCapacity: Int,
    players: Seq[Player],
    round: Round,
    rules: RuleSet)
extends WithOptId[Long, GameSession]
{
    override def withId(id: Long): GameSession =
        this.copy(id = Some(id))
}