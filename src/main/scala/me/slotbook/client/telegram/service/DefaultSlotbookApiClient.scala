package me.slotbook.client.telegram.service

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.osinka.i18n.Lang
import com.typesafe.scalalogging.Logger
import me.slotbook.client.telegram.model.slotbook._
import play.api.libs.json.JsValue
import play.api.libs.ws.DefaultWSCookie
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.Future

trait SlotbookApiClient {
  type Lat = BigDecimal
  type Lng = BigDecimal

  /**
    * Returns list of service categories.
    *
    * @return map of service id -> service name.
    */
  def listCategories(lang: Lang): Future[Seq[Service]]

  def listCategoryServices(categoryId: Int)(implicit lang: Lang): Future[Seq[ServiceWithCompaniesCount]]

  def listCompaniesByService(serviceId: Service.ID, location: Option[Location])(implicit lang: Lang): Future[Seq[CompanyDistanceRating]]

  def listEmployeesByCompany(companyId: Company.ID)(implicit lang: Lang): Future[Seq[UserWithRating]]

  def listSlots(serviceId: Service.ID, employeeId: User.ID, date: String)(implicit lang: Lang): Future[Seq[Period]]

  def bindSlot(slotId: Timeslot.ID)(implicit lang: Lang): Future[Unit]
}

class DefaultSlotbookApiClient extends SlotbookApiClient {

  import play.api.libs.ws.JsonBodyReadables._

  import scala.concurrent.ExecutionContext.Implicits._

  val apiUrl = "http://127.0.0.1:9000/api"
  val langCookies = "PLAY_LANG"
  val defaultSearchDistance = 20
  // distance in kilometers
  val defaultSearchCount = 100 // limit companies to search

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
  override def listCategories(lang: Lang): Future[Seq[Service]] = {
    wsClient.url(s"$apiUrl/categories")
      .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
      .get()
      .map { response =>
        if (response.status == 200) {
          response.body[JsValue].validate[Seq[Service]].asOpt.getOrElse(Seq())
        } else {
          println(s"Unable to get categories $response")
          Seq()
        }
      }
  }

  /**
    * Returns services by specific category.
    *
    * @param categoryId id of the service category.
    * @return
    */
  override def listCategoryServices(categoryId: Int)(implicit lang: Lang): Future[Seq[ServiceWithCompaniesCount]] = {
    wsClient
      .url(s"$apiUrl/categories/$categoryId/services")
      .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
      .get()
      .map { response =>
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
  override def listCompaniesByService(serviceId: Service.ID, location: Option[Location])(implicit lang: Lang): Future[Seq[CompanyDistanceRating]] = {
    val url = location.map {
      loc =>
        wsClient.url(s"$apiUrl/services/$serviceId/companies/location")
          .withQueryStringParameters("lat" -> loc.lat.toString(), "lng" -> loc.lng.toString(), "distance" -> defaultSearchDistance.toString, "limit" -> defaultSearchCount.toString)
    }.getOrElse {
      wsClient.url(s"$apiUrl/services/$serviceId/companies")
    }

    Logger.apply("Test").debug(s"url: $url")

    url.addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
      .get()
      .map { response =>
        response.body[JsValue].validate[Seq[CompanyDistanceRating]].asOpt.getOrElse(Seq())
      }
  }

  /**
    * Returns list of employees of specified company.
    *
    * @param companyId id of the company
    * @return
    */
  override def listEmployeesByCompany(companyId: Company.ID)(implicit lang: Lang): Future[Seq[UserWithRating]] = {
    wsClient
      .url(s"$apiUrl/companies/$companyId/employees")
      .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
      .get().map { response =>
      response.body[JsValue].validate[Seq[UserWithRating]].asOpt.getOrElse(Seq())
    }
  }

  override def listSlots(serviceId: Service.ID, employeeId: User.ID, date: String)(implicit lang: Lang): Future[Seq[Period]] = {
    wsClient
      .url(s"$apiUrl/employees/$employeeId/slots/$serviceId/$date/1")
      .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
      .get()
      .map { response =>
        response.body[JsValue].validate[Seq[DateWithTimeslot]].asOpt match {
          case Some(Seq(data)) => data.periods
          case None => Seq()
        }
      }
  }

  override def bindSlot(slotId: Timeslot.ID)(implicit lang: Lang): Future[Unit] = {
    Future.successful()
  }
}
