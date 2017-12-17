package org.natasemka.citadel.model

sealed trait Color {
  def id: String
}

case object Red extends Color { val id = "red" }
case object Green extends Color { val id = "green" }
case object Yellow extends Color { val id = "yellow" }
case object Purple extends Color { val id = "purple" }
case object Blue extends Color { val id = "blue" }
