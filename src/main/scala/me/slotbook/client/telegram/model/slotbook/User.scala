package me.slotbook.client.telegram.model.slotbook

import play.api.libs.json.{Format, Json}

case class User(id: User.ID, firstName: User.Name, lastName: User.Name)

case class UserWithRating(user: User, rating: Int)

object User {
  type ID = String
  type Name = String

  implicit val format: Format[User] = Json.format[User]
}

object UserWithRating {
  implicit val format: Format[UserWithRating] = Json.format[UserWithRating]
}