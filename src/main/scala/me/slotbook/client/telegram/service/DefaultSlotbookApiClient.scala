package me.slotbook.client.telegram.service

import play.api.libs.json._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.osinka.i18n.Lang
import com.typesafe.scalalogging.Logger
import info.mukel.telegrambot4s.models.ChatId
import me.slotbook.client.telegram.model.slotbook.{UserData, _}
import me.slotbook.client.telegram.service.CompaniesSearchParameters.{defaultSearchCount, defaultSearchDistance}
import org.joda.time.{LocalDate, LocalTime}
import play.api.libs.ws.DefaultWSCookie
import play.api.libs.ws.ahc.StandaloneAhcWSClient
import play.api.libs.ws.JsonBodyWritables._
import play.shaded.ahc.org.asynchttpclient.util.HttpConstants.ResponseStatusCodes
import play.api.libs.json.Json

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

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

  def listCompaniesByService(serviceId: Service.ID, location: Option[LatLng], searchRadius: Option[Int])(implicit lang: Lang): Future[Seq[CompanyDistanceRating]]

  def listEmployeesByCompany(companyId: Company.ID, serviceId: Service.ID)(implicit lang: Lang): Future[Seq[UserWithRating]]

  def listSlots(serviceId: Service.ID, employeeId: User.ID, date: String)(implicit lang: Lang): Future[Seq[Timeslot]]

  def bindSlot(employeeId: User.ID, companyId: Company.ID, slotDate: LocalDate, slotTimes: Timeslot.Times,
               user: info.mukel.telegrambot4s.models.User)(implicit lang: Lang): Future[Unit]

  def getHistoryOfVisits(user: info.mukel.telegrambot4s.models.User)(implicit lang: Lang): Future[Seq[PeriodWithUser]]

  def calendar(user: info.mukel.telegrambot4s.models.User)(implicit lang: Lang): Future[Seq[PeriodWithUser]]
}

object CompaniesSearchParameters {
  val defaultSearchDistance = 20
  // distance in kilometers
  val defaultSearchCount = 100 // limit companies to search
}

class DefaultSlotbookApiClient extends SlotbookApiClient {

  import play.api.libs.ws.JsonBodyReadables._

  import scala.concurrent.ExecutionContext.Implicits._

  //val apiUrl = "http://127.0.0.1:9000/api"
  val apiUrl = "http://206.189.3.71/api"
  val langCookies = "PLAY_LANG"

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
                println(s"Unable to get categories $response. Returning empty list.")
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
  override def listCompaniesByService(serviceId: Service.ID,
                                      location: Option[LatLng],
                                      searchRadius: Option[Int] = Some(defaultSearchDistance))
                                     (implicit lang: Lang): Future[Seq[CompanyDistanceRating]] = {
    val url = location.map { loc =>
      wsClient.url(s"$apiUrl/services/$serviceId/companies/location")
              .withQueryStringParameters("lat" -> loc.lat.toString(),
                "lng" -> loc.lng.toString(),
                "distance" -> searchRadius.toString,
                "limit" -> defaultSearchCount.toString)
    }.getOrElse {
      wsClient.url(s"$apiUrl/services/$serviceId/companies")
    }

    Logger.apply("Test").debug(s"url: $url")

    url.addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
            .get()
            .map { response =>
              if (response.status != 200) {
                println(s"Error while executing request ${response.statusText}")
                Seq()
              } else {
                response.body[JsValue].validate[Seq[CompanyDistanceRating]].asOpt.getOrElse(Seq())
              }
            }
  }

  /**
    * Returns list of employees of specified company.
    *
    * @param companyId id of the company
    * @return
    */
  override def listEmployeesByCompany(companyId: Company.ID, serviceId: Service.ID)(implicit lang: Lang): Future[Seq[UserWithRating]] = {
    wsClient
            .url(s"$apiUrl/services/$serviceId/companies/$companyId/employees")
            .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
            .get()
            .map { response =>
              response.body[JsValue].validate[Seq[UserWithRating]].asOpt.getOrElse(Seq())
            }
  }

  override def listSlots(serviceId: Service.ID, employeeId: User.ID, date: String)(implicit lang: Lang): Future[Seq[Timeslot]] = {
    wsClient
            .url(s"$apiUrl/employees/$employeeId/slots/$serviceId/$date/1")
            .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
            .get()
            .map { response =>
              if (response.status == ResponseStatusCodes.OK_200) {
                response.body[JsValue].validate[Seq[DateWithTimeslot]].getOrElse(Seq()).flatMap(_.periods)
              } else {
                println(s"Error while requesting list of slots, response: [$response]. Returning empty list")
                Seq()
              }
            }
  }

  override def bindSlot(employeeId: User.ID, companyId: Company.ID, slotDate: LocalDate, slotTimes: Timeslot.Times,
                        user: info.mukel.telegrambot4s.models.User)
                       (implicit lang: Lang): Future[Unit] = {
    // in case user already registered in the system - login first
    authenticate(user, user.id).map {
      case Some(token) => bind(slotDate, slotTimes, user, token)
      case None => println("Unable to perform binding because of user was not authenticated")
    }

    def bind(slotDate: LocalDate, slotTimes: Timeslot.Times, user: info.mukel.telegrambot4s.models.User, token: String) = {
      wsClient
              .url(s"$apiUrl/companies/$companyId/employees/$employeeId/events")
              .withHttpHeaders(
                "X-Auth-Token" -> token,
                "Content-Type" -> "application/json; charset=utf-8",
                "Csrf-Token" -> "nocheck"
              )
              .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
              .post(
                new Event(
                  startDate = slotDate,
                  endDate = slotDate,
                  startTime = slotTimes._1,
                  endTime = slotTimes._2
                ).toJson).map { response =>
        println(response)
      }
    }

    Future.successful("")
  }

  override def getHistoryOfVisits(user: info.mukel.telegrambot4s.models.User)(implicit lang: Lang): Future[Seq[PeriodWithUser]] = {
    login(user).flatMap {
      case Success(token) =>
        wsClient.url(s"$apiUrl/events/history")
                .addHttpHeaders("X-Auth-Token" -> token)
                .addCookies(DefaultWSCookie(langCookies, lang.locale.getLanguage))
                .get().map { response =>
          println(response)

          response.body[JsValue]
                  .validate[Seq[Event]].asOpt.getOrElse(Seq())
                  .map(e => PeriodWithUser(e, UserWithRating(null, 5)))
        }
      case Failure(exception) =>
        println(exception)

        Future.successful(Seq())
    }
  }

  override def calendar(user: info.mukel.telegrambot4s.models.User)(implicit lang: Lang): Future[Seq[PeriodWithUser]] = ???

  private def login(user: info.mukel.telegrambot4s.models.User): Future[Try[String]] = {
    wsClient.url(s"$apiUrl/auth/login")
            .post(UserLoginData.of(user).toJson).map { response =>
      println(response)

      if (response.status == ResponseStatusCodes.OK_200) {
        Success(response.body[JsValue].\("message").as[String])
      } else {
        Failure(new RuntimeException("Unable to register a user"))
      }
    }
  }

  private def register(user: info.mukel.telegrambot4s.models.User, chatId: Long): Future[Try[String]] = {
    println(s"registering $user")

    wsClient.url(s"$apiUrl/auth/account/create")
            .addHttpHeaders("Content-Type" -> "application/json")
            .post(UserData.of(user, chatId).toJson).map { response =>

      println(response)

      if (response.status == ResponseStatusCodes.OK_200) {
        Success(response.body[JsValue].\("message").as[String])
      } else {
        Failure(new RuntimeException("Unable to register a user"))
      }
    }
  }

  def authenticate(user: info.mukel.telegrambot4s.models.User, chatId: Long): Future[Option[String]] = {
    login(user).flatMap {
      case Success(token) => Future.successful(Some(token))
      case Failure(_) => register(user, chatId).map {
        case Success(token) => Some(token)
        case Failure(_) => None
      }
    }
  }

  def doAsAuthenticated[U, T](user: info.mukel.telegrambot4s.models.User, isAuthenticated: U => T, isNotAuthenticated: => T): T = ???
}
