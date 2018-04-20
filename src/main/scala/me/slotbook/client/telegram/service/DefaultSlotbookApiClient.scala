package me.slotbook.client.telegram.service

import org.joda.time.LocalDate

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

  /**
    * This type represents an Id of the company.
    */
  type CompanyId = Int

  /**
    * This type represents a name of the company.
    */
  type CompanyName = String

  type Company = (CompanyId, CompanyName)

  type EmployeeId = String

  type EmployeeName = String

  type EmployeeRating = Int

  type Employee = (EmployeeId, EmployeeName, EmployeeRating)

  type PeriodId = Int

  /**
    * This type represents a timeslot
    */
  type Timeslot = (PeriodId, String)

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories: Future[Map[ServiceId, ServiceName]]

  def listCategoryServices(categoryId: Int): Future[Map[ServiceId, ServiceName]]

  def listCompaniesByService(serviceId: ServiceId, location: String): Future[Seq[(CompanyId, CompanyName)]]

  def listEmployeesByCompany(companyId: CompanyId): Future[Seq[Employee]]

  def listSlots(serviceId: ServiceId, companyId: CompanyId, employeeId: EmployeeId, date: LocalDate): Future[Seq[Timeslot]]

  def bindSlot(slotId: PeriodId): Future[Timeslot]
}

class DefaultSlotbookApiClient extends SlotbookApiClient {

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories: Future[Map[ServiceId, ServiceName]] =
    Future.successful(Map(1 -> "Category 1", 2 -> "Category 2", 3 -> "Category 3"))

  /**
    *
    * @param categoryId
    * @return
    */
  def listCategoryServices(categoryId: Int): Future[Map[ServiceId, ServiceName]] = {
    Future.successful(Map(1 -> "Service 1", 2 -> "Service 2", 3 -> "Service 3"))
  }

  /**
    *
    * @param serviceId
    * @param location
    * @return
    */
  def listCompaniesByService(serviceId: ServiceId, location: String): Future[Seq[(CompanyId, CompanyName)]] = {
    Future.successful(Seq(1 -> "Company 1", 2 -> "Company 2", 3 -> "Company 3"))
  }

  /**
    *
    * @param companyId
    * @return
    */
  override def listEmployeesByCompany(companyId: CompanyId): Future[Seq[(EmployeeId, EmployeeName, EmployeeRating)]] = {
    Future.successful(Seq(("id1", "Employee 1", 1), ("id2", "Employee 2", 2), ("id3", "Employee 3", 3)))
  }

  override def listSlots(serviceId: ServiceId, companyId: CompanyId, employeeId: EmployeeId, date: LocalDate): Future[Seq[(PeriodId, String)]] = {
    Future.successful(Seq(1 -> "12:00", 2 -> "13:00", 3 -> "14:00"))
  }

  override def bindSlot(slotId: PeriodId): Future[(PeriodId, String)] = {
    Future.successful(slotId, "13:00")
  }
}
