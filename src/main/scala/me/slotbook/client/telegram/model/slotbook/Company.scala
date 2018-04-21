package me.slotbook.client.telegram.model.slotbook

import me.slotbook.client.telegram.model.slotbook.Company.{ID, Name}
import play.api.libs.json.{Format, Json}

case class Company(id: ID, name: Name)

case class CompanyDistanceRating(company: Company, distance: Int, rating: Int)

object Company {
  type ID = Int
  type Name = String

  implicit val format: Format[Company] = Json.format[Company]
}

object CompanyDistanceRating {
  implicit val format: Format[CompanyDistanceRating] = Json.format[CompanyDistanceRating]
}
