package me.slotbook.client.telegram.model.slotbook

import me.slotbook.client.telegram.model.slotbook.User.loginId
import play.api.libs.json.{ Format, JsValue, Json }
import play.api.libs.ws.DefaultBodyWritables

case class UserData(loginId: String, firstName: String, email: Option[String] = None, phone: Option[String] = None, telegram: String) extends DefaultBodyWritables {
  def toJson: JsValue = UserData.jsonFormat.writes(this)
}

case class UserLoginData(email: String, password: String, rememberMe: Boolean = true) extends DefaultBodyWritables {
  def toJson: JsValue = UserLoginData.jsonFormat.writes(this)
}

object UserData {
  implicit val jsonFormat: Format[UserData] = Json.format[UserData]

  def of(user: info.mukel.telegrambot4s.models.User, chatId: Long): UserData = {
    UserData(loginId = loginId(user), firstName = user.firstName, email = None, phone = None, telegram = chatId.toString)
  }
}

object UserLoginData {
  implicit val jsonFormat: Format[UserLoginData] = Json.format[UserLoginData]

  def of(user: info.mukel.telegrambot4s.models.User): UserLoginData = {
    UserLoginData(loginId(user), User.DEFAULT_PASSWORD)
  }
}
