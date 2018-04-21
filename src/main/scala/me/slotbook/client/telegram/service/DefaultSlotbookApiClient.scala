package me.slotbook.client.telegram.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import me.slotbook.client.telegram.model.slotbook._
import org.joda.time.LocalDate
import play.api.libs.json.JsValue
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

trait SlotbookApiClient {
  type Lat = BigDecimal
  type Lng = BigDecimal

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
  def listCategories: Future[Seq[Service]]

  def listCategoryServices(categoryId: Int): Future[Seq[ServiceWithCompaniesCount]]

  def listCompaniesByService(serviceId: Service.ID, location: Location): Future[Seq[CompanyDistanceRating]]

  def listEmployeesByCompany(companyId: Company.ID): Future[Seq[UserWithRating]]

  def listSlots(serviceId: Service.ID, companyId: Company.ID, employeeId: User.ID, date: LocalDate): Future[Seq[Timeslot]]

  def bindSlot(slotId: PeriodId): Future[Timeslot]
}

class DefaultSlotbookApiClient extends SlotbookApiClient {

  import play.api.libs.ws.JsonBodyReadables._

  import scala.concurrent.ExecutionContext.Implicits._

  val apiUrl = "http://127.0.0.1:9000/api"
  implicit val system = ActorSystem()

  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val wsClient = StandaloneAhcWSClient()

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories: Future[Seq[Service]] = {
    wsClient.url(s"$apiUrl/categories").get().map { response =>
      response.body[JsValue].validate[Seq[Service]].asOpt.getOrElse(Seq())
    }
  }

  /**
    * Returns services by specific category.
    *
    * @param categoryId id of the service category.
    * @return
    */
  def listCategoryServices(categoryId: Int): Future[Seq[ServiceWithCompaniesCount]] = {
    wsClient.url(s"$apiUrl/categories/$categoryId/services").get().map { response =>
      response.body[JsValue].validate[Seq[ServiceWithCompaniesCount]].asOpt.getOrElse(Seq())
    }
  }

  /**
    * Returns list of companies by specified service and location.
    *
    * @param serviceId id of the service.
    * @param location  location to filter.
    * @return
    */
  def listCompaniesByService(serviceId: Service.ID, location: Location): Future[Seq[CompanyDistanceRating]] = {
    wsClient.url(s"$apiUrl/services/$serviceId/companies/location")
      .withQueryStringParameters("lat" -> location.lat.toString(), "lng" -> location.lng.toString(), "distance" -> "20", "limit" -> "100")
      .get()
      .map { response =>
        println(response.body)
        response.body[JsValue].validate[Seq[CompanyDistanceRating]].asOpt.getOrElse(Seq())
      }
  }

  /**
    * Returns list of employees of specified company.
    *
    * @param companyId id of the company
    * @return
    */
  override def listEmployeesByCompany(companyId: Company.ID): Future[Seq[UserWithRating]] = {
    wsClient.url(s"$apiUrl/companies/$companyId/employees").get().map { response =>
      println(response.body)

      response.body[JsValue].validate[Seq[UserWithRating]].asOpt.getOrElse(Seq())
    }
  }

  override def listSlots(serviceId: Service.ID, companyId: Company.ID, employeeId: User.ID, date: LocalDate): Future[Seq[(PeriodId, String)]] = {
    Future.successful(Seq(1 -> "12:00", 2 -> "13:00", 3 -> "14:00"))
  }

  override def bindSlot(slotId: PeriodId): Future[(PeriodId, String)] = {
    Future.successful(slotId, "13:00")
  }
}
