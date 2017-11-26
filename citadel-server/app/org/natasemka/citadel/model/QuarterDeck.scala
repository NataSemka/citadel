package org.natasemka.citadel.model

case class QuarterDeck(
    availableQuarters: Seq[Quarter],
    playedQuarters: Seq[Quarter]
    )