package org.natasemka.citadel.model

sealed trait Color {
  def id: String
}

object Color {
  def of(id: String): Color = id match {
    case "red" => Red
    case "green" => Green
    case "yellow" => Yellow
    case "purple" => Purple
    case "blue" => Blue
  }
}

case object Red extends Color { val id = "red" }
case object Green extends Color { val id = "green" }
case object Yellow extends Color { val id = "yellow" }
case object Purple extends Color { val id = "purple" }
case object Blue extends Color { val id = "blue" }
