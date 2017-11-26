package org.natasemka.citadel.model

case class CharacterDeck(
    hiddenCharacters: Seq[Character],
    displayedCharacters: Seq[Character],
    availableCharacters: Seq[Character]
    )