package me.slotbook.client.telegram.service

import scala.concurrent.Future

class SlotbookApiClient {
  /**
    * This type represents an Id of a service.
    */
  type ServiceId = Int

  /**
    * This type represents a Service. It provides an Id and a Name.
    */
  type Service = (ServiceId, String)

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories: Future[Map[ServiceId, String]] =
    Future.successful(Map(1 -> "Test1", 2 -> "Test2"))

  def listCategoryServices: Future[Map[ServiceId, String]] = ???

  def listCompaniesByService(serviceId: ServiceId, location: String) = ???
}
