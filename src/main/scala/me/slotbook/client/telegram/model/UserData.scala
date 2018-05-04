package me.slotbook.client.telegram.model

import info.mukel.telegrambot4s.models.User
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.DefaultBodyWritables

case class UserData(firstName: String, email: Option[String] = None, phone: Option[String] = None) extends DefaultBodyWritables {
  def toJson: JsValue = UserData.jsonFormat.writes(this)
}

object UserData {
  implicit val jsonFormat: Format[UserData] = Json.format[UserData]

  def of(user: User): UserData = {
    UserData(firstName = user.firstName, email = None, phone = None)
  }
}