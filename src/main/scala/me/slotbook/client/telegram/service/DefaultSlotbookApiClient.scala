package me.slotbook.client.telegram.service

import scala.concurrent.Future

trait SlotbookApiClient {
  /**
    * This type represents an Id of a service.
    */
  type ServiceId = Int

  /**
    * This type represents the name of a service.
    */
  type ServiceName = String

  /**
    * This type represents a Service. It provides an Id and a Name.
    */
  type Service = (ServiceId, ServiceName)
}

class DefaultSlotbookApiClient extends SlotbookApiClient {

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories: Future[Map[ServiceId, ServiceName]] =
    Future.successful(Map(1 -> "Category 1", 2 -> "Category 2", 3 -> "Category 3"))

  def listCategoryServices: Future[Map[ServiceId, ServiceName]] = {
    Future.successful(Map(1 -> "Service 1", 2 -> "Service 2", 3 -> "Service 3"))
  }

  def listCompaniesByService(serviceId: ServiceId, location: String) = ???
}
