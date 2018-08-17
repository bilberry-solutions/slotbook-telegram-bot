package me.slotbook.client.telegram.model.slotbook

import play.api.libs.json.{Format, Json}

case class Service(id: Service.ID, name: Service.Name)

case class ServiceWithCompaniesCount(service: Service, companiesCount: Int)

object Service {
  type ID = Int

  type Name = String

  implicit val format: Format[Service] = Json.format[Service]
}

object ServiceWithCompaniesCount {
  implicit val format: Format[ServiceWithCompaniesCount] = Json.format[ServiceWithCompaniesCount]
}
