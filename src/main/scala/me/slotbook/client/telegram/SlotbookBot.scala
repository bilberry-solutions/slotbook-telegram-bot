package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.dao.{InMemoryStateDao, StateDao}
import me.slotbook.client.telegram.model._
import me.slotbook.client.telegram.model.slotbook.Timeslot
import me.slotbook.client.telegram.model.slotbook.Timeslot.dateFormatter
import me.slotbook.client.telegram.service.DefaultSlotbookApiClient
import org.joda.time.{LocalDate, LocalDateTime}

import scala.concurrent.Future

class SlotbookBot(tok: String) extends TelegramBot with Polling with Commands with Callbacks {
  override def token: String = tok

  val CATEGORY_TAG = "category_"
  val SERVICE_TAG = "service_"
  val COMPANY_TAG = "company_"
  val EMPLOYEE_TAG = "employee_"
  val SLOT_TAG = "slot_"
  val DATE_TAG = "date_"

  val slotbookApiClient: DefaultSlotbookApiClient = new DefaultSlotbookApiClient()
  val stateDao: StateDao = new InMemoryStateDao()

  onCommand('help) { implicit msg =>
    reply("Just use /find")
  }

  onCommand('users) { implicit msg =>
    stateDao.getAll.map { userIds =>
      reply(userIds.mkString(","))
    }
  }

  onCommand('find) { implicit msg =>
    println(msg.from)

    msg.from match {
      case Some(user) => stateDao.registerUser(user)
      case None => Future.failed(new RuntimeException("Unable to register anonymous user"))
    }

    reply(
      text = AskForClientLocation().message,
      replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(AskForClientLocation().message)))).map { message =>
      println(message)
    }
  }

  onCommand('category) { implicit msg =>
    slotbookApiClient.listCategories.map { categories =>
      val msf = AskForServiceCategory(categories, prefixTag(CATEGORY_TAG))
      reply(msf.message, replyMarkup = msf.markup)
    }
  }

  onMessage { implicit msg =>
    if (msg.location.isDefined) {
      println(s"User's location: ${msg.location}")

      // update user's location
    }
  }

  onCallbackWithTag(CATEGORY_TAG) { implicit callback =>
    ackCallback(text = Some("Category has been accepted"))

    callback.data match {
      case Some(categoryId) =>
        println(s"category: $categoryId")
        callback.message.map { message =>
          slotbookApiClient.listCategoryServices(categoryId.toInt).map { services =>
            val rpl = AskForClientService(services, prefixTag(SERVICE_TAG))

            reply(rpl.message, replyMarkup = rpl.markup)(message)
          }
        }

      case None => println("Category was not selected")
    }
  }

  /* Handling service selection */
  onCallbackWithTag(SERVICE_TAG) { implicit callback =>
    println(s"category: $callback")

    ackCallback(text = Some("Service has been accepted"))

    callback.data match {
      case Some(serviceId) =>
        println(s"service: $serviceId")
        callback.message.map { message =>
          slotbookApiClient.listCompaniesByService(serviceId.toInt, model.slotbook.Location(BigDecimal(50.434171), BigDecimal(30.485722))).map { companies =>
            val rpl = AskForCompany(companies, prefixTag(COMPANY_TAG))

            reply(rpl.message, replyMarkup = rpl.markup)(message)
          }
        }
      case None => println("Service was not selected")
    }
  }

  /* Handling company selection */
  onCallbackWithTag(COMPANY_TAG) { implicit callback =>
    ackCallback(text = Some("Company has been accepted"))

    callback.data match {
      case Some(companyId) =>
        println(s"company: $companyId")
        callback.message.map { message =>
          slotbookApiClient.listEmployeesByCompany(companyId.toInt).map { employees =>
            val rpl = AskForEmployee(employees, prefixTag(EMPLOYEE_TAG))

            reply(rpl.message, replyMarkup = rpl.markup)(message)
          }
        }
      case None => println("Employee was not selected")
    }
  }

  /* Handling company selection */
  onCallbackWithTag(EMPLOYEE_TAG) { implicit callback =>
    ackCallback(text = Some("Employee has been accepted"))

    callback.data match {
      case Some(employeeId) =>
        println(s"service: $employeeId")
        callback.message.map { message =>
          val today = LocalDate.now.toDateTimeAtStartOfDay
          val tomorrow = today.plusDays(1)
          val afterTomorrow = today.plusDays(2)

          val rpl = AskForDates(Seq(today, tomorrow, afterTomorrow).map(dateFormatter.print(_)), prefixTag(DATE_TAG))

          reply(rpl.message, replyMarkup = rpl.markup)(message)
        }
      case None => println("Date was not selected")
    }
  }

  onCallbackWithTag(DATE_TAG) { implicit callback =>
    ackCallback(text = Some("Date has been accepted"))

    callback.data match {
      case Some(date) =>
        println(s"date: $date")
        callback.message.map { message =>
          slotbookApiClient.listSlots(242, 1, "7196bc99-8f93-4c03-9982-0b4f40ddebec", date).map { slots =>
            println(slots)
            val rpl = AskForSlot(slots, prefixTag(SLOT_TAG))

            reply(rpl.message, replyMarkup = rpl.markup)(message)
          }
        }
      case None => println("Timeslot was not selected")
    }
  }


  onCallbackWithTag(SLOT_TAG) { implicit callback =>
    ackCallback(text = Some("Slot has been accepted"))

    callback.data match {
      case Some(slotId) =>
        println(s"slot: $slotId")
        callback.message.map { message =>
          slotbookApiClient.bindSlot(slotId.toInt).map { _ =>
            reply("Event has been created")(message)
          }
        }
      case None => println("Slot was not selected")
    }
  }

  override def receiveMessage(msg: Message): Unit = {
    super.receiveMessage(msg)

    //println(msg)
  }

  override def receiveCallbackQuery(callbackQuery: CallbackQuery): Unit = {
    super.receiveCallbackQuery(callbackQuery)

    //println(callbackQuery)
  }

  override def run(): Unit = {
    super.run()

    println("Bot has been started")
  }
}
