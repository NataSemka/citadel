package org.natasemka.citadel.model

case class Quarter(
    name: String,
    description: String,
    properties: Seq[QuarterProperty],
    color: Color,
    buildCost: Int,
    victoryPoints: Int
    )