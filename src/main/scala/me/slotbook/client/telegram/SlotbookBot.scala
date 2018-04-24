package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.model._
import me.slotbook.client.telegram.model.slotbook.Location
import me.slotbook.client.telegram.model.slotbook.Timeslot.{dateFormatter, dateTimeFormatter}
import me.slotbook.client.telegram.service.{DefaultSlotbookApiClient, StateService}
import org.joda.time.LocalDate

class SlotbookBot(val token: String) extends TelegramBot with Polling with Commands with Callbacks {
  val CATEGORY_TAG = "category_"
  val SERVICE_TAG = "service_"
  val COMPANY_TAG = "company_"
  val EMPLOYEE_TAG = "employee_"
  val SLOT_TAG = "slot_"
  val DATE_TAG = "date_"
  val MENU_TAG = "help_"

  val slotbookApiClient: DefaultSlotbookApiClient = new DefaultSlotbookApiClient()
  val stateService = StateService()

  onCommand('menu) { implicit msg =>
    reply(text = "Help", replyMarkup = AskForMenuAction(prefixTag(MENU_TAG)).markup)
  }

  onCommand('find) { implicit msg =>
    /*println(msg.from)

    msg.from match {
      case Some(user) => stateService.update(stateService.current.copy(userId = Some(user.id)))
      case None => Future.failed(new RuntimeException("Unable to register anonymous user"))
    }*/

    reply(
      text = AskForClientLocation().message,
      replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(AskForClientLocation().message)))).map { message =>
    }
  }

  onCommand('category) { implicit msg =>
    slotbookApiClient.listCategories.map { categories =>
      val msf = AskForServiceCategory(categories, prefixTag(CATEGORY_TAG))
      reply(msf.message, replyMarkup = msf.markup)
    }
  }

  onMessage { implicit msg =>
    if (msg.location.isDefined && msg.from.isDefined) {
      val loc = msg.location.map(loc => Location(BigDecimal(loc.latitude), BigDecimal(loc.longitude)))
      stateService.updateLocation(msg.from.get.id, loc)
    }
  }

  onCallbackWithTag(MENU_TAG) { implicit callback =>
    ackCallback(text = Some("Menu item has been selected"))

    println(callback)
  }

  onCallbackWithTag(CATEGORY_TAG) { implicit callback =>
    ackCallback(text = Some("Category has been accepted"))

    callback.data match {
      case Some(categoryId) =>
        stateService.updateCategory(callback.from.id, categoryId.toInt)
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
    ackCallback(text = Some("Service has been accepted"))

    callback.data match {
      case Some(serviceId) =>
        stateService.updateService(callback.from.id, serviceId.toInt)
        callback.message.map { message =>
          slotbookApiClient.listCompaniesByService(serviceId.toInt, stateService.current(callback.from.id).location).map { companies =>
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
        stateService.updateCompany(callback.from.id, companyId.toInt)
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
        stateService.updateEmployee(callback.from.id, employeeId)

        callback.message.map { message =>
          val today = LocalDate.now.toDateTimeAtStartOfDay
          val tomorrow = today.plusDays(1)
          val afterTomorrow = today.plusDays(2)

          // TODO check why api returns slots in the past
          val rpl = AskForDates(Seq(today, tomorrow, afterTomorrow)
            .map(d => (dateTimeFormatter.print(d), dateFormatter.print(d))), prefixTag(DATE_TAG))

          reply(rpl.message, replyMarkup = rpl.markup)(message)
        }

      case None => println("Date was not selected")
    }
  }

  onCallbackWithTag(DATE_TAG) { implicit callback =>
    ackCallback(text = Some("Date has been accepted"))

    callback.data match {
      case Some(date) =>
        callback.message.map { message =>
          if (stateService.current(callback.from.id).serviceId.isDefined && stateService.current(callback.from.id).employeeId.isDefined) {
            slotbookApiClient.listSlots(stateService.current(callback.from.id).serviceId.get, stateService.current(callback.from.id).employeeId.get, date).map { slots =>
              if (slots.nonEmpty) {
                val rpl = AskForSlot(slots, prefixTag(SLOT_TAG))
                reply(rpl.message, replyMarkup = rpl.markup)(message)
              } else {
                reply("There are no free slots on this date")(message)
              }
            }
          } else {
            reply("Please select a service and employee to register a visit")(message)
          }
        }
      case None => println("Timeslot was not selected")
    }
  }


  onCallbackWithTag(SLOT_TAG) { implicit callback =>
    ackCallback(text = Some("Slot has been accepted"))

    callback.data match {
      case Some(slotId) =>
        stateService.updateEmployee(callback.from.id, slotId)

        callback.message.map { message =>
          slotbookApiClient.bindSlot(slotId.toInt).map { slots =>
            reply("Event has been created")(message)
          }
        }
      case None => println("Slot was not selected")
    }
  }

  onCommand('reset) { implicit message =>
    if (message.from.isDefined) {
      stateService.reset(message.from.get.id).map { _ => reply("Search reset successful") }
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
