package me.slotbook.client.telegram.model

sealed trait Reply {
  def message: String
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"
}
