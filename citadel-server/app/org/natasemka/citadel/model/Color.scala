package org.natasemka.citadel.model

sealed trait Color {
  def id: String
}

case object Red extends Color { val id = "Red" }
case object Green extends Color { val id = "Green" }
case object Yellow extends Color { val id = "Yellow" }
case object Purple extends Color { val id = "Purple" }
case object Blue extends Color { val id = "Blue" }
