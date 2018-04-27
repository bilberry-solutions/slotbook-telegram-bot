package me.slotbook.client.telegram

import com.osinka.i18n.Lang
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.model.Tags._
import me.slotbook.client.telegram.model._
import me.slotbook.client.telegram.model.slotbook.Location
import me.slotbook.client.telegram.model.slotbook.Timeslot.{dateFormatter, dateTimeFormatter}
import me.slotbook.client.telegram.service.{DefaultSlotbookApiClient, StateService}
import org.joda.time.LocalDate

import scala.concurrent.Future

class SlotbookBot(val token: String) extends TelegramBot with Polling with Commands with Callbacks {

  val slotbookApiClient: DefaultSlotbookApiClient = new DefaultSlotbookApiClient()
  val stateService = StateService()

  onCommand('menu) { implicit msg =>
      replyWithNew(AskForMenuAction(prefixTag(MENU_TAG), language(msg)), msg)
  }

  onMessage { implicit msg =>
    if (msg.location.isDefined && msg.from.isDefined) {
      val loc = msg.location.map(loc => Location(BigDecimal(loc.latitude), BigDecimal(loc.longitude)))
      stateService.updateLocation(msg.from.get.id, loc)
    }
  }

  onCallbackWithTag(MENU_TAG) { implicit callback =>
    ackCallback(text = Some("Menu item has been selected"))

    callback.message.map { message =>
      callback.data.map(_.toInt) match {
        case Some(AskForMenuAction.START_SEARCH_ACTION_ID) =>
          slotbookApiClient.listCategories.map { categories =>
            replyWithNew(AskForServiceCategory(categories, prefixTag(CATEGORY_TAG)), message)
          }
        case Some(AskForMenuAction.CHANGE_LANG_ID) =>
          replyWithNew(AskForNewLanguage(prefixTag(LANGUAGE_TAG)), message)
        case Some(AskForMenuAction.HELP_ACTION_ID) =>
          replyWithNew(HelpReply(), message)
        case Some(AskForMenuAction.RESET_SEARCH_ACTION_ID) =>
          stateService.reset(callback.from.id).map(_ => reply("Search reset successful")(message))
      }
    }
  }

  onCallbackWithTag(LANGUAGE_TAG) { implicit callback =>
    ackCallback(text = Some("Language has been changed"))

    println(callback.data)
    callback.message match {
      case Some(message) =>
        callback.data match {
          case Some(lang) =>
            stateService.updateLanguage(callback.from.id, Lang(lang))
            replyOverriding(AskForMenuAction(prefixTag(MENU_TAG), Lang(lang)), message)
          case None => reply("Unknown language specified")(message)
        }
      case None => println("Unable to extract message from callback")
    }
  }

  onCallbackWithTag(CATEGORY_TAG) { implicit callback =>
    ackCallback(text = Some("Category has been accepted"))

    callback.data match {
      case Some(categoryId) =>
        stateService.updateCategory(callback.from.id, categoryId.toInt)
        callback.message.map { message =>
          slotbookApiClient.listCategoryServices(categoryId.toInt).map { services =>
            replyWithNew(AskForClientService(services, prefixTag(SERVICE_TAG)), message)
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
            replyWithNew(AskForCompany(companies, prefixTag(COMPANY_TAG)), message)
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
            replyWithNew(AskForEmployee(employees, prefixTag(EMPLOYEE_TAG)), message)
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

          replyWithNew(rpl, message)
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
                replyWithNew(AskForSlot(slots, prefixTag(SLOT_TAG)), message)
              } else {
                replyWithNew(Errors.NoSlots(), message)
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
            replyWithNew(EventCreated(), message)
          }
        }
      case None => println("Slot was not selected")
    }
  }

  def replyOverriding(reply: Reply, message: Message): Future[Either[Boolean, Message]] = {
    request(
      EditMessageReplyMarkup(Some(ChatId(message.source)),
        Some(message.messageId),
        replyMarkup = reply.markup.map(_.asInstanceOf[InlineKeyboardMarkup])))
  }

  def replyWithNew(rpl: Reply, message: Message): Future[Message] = {
    reply(rpl.message, replyMarkup = rpl.markup)(message)
  }

  def language(message: Message): Lang = {
    message.from.map(_.id).flatMap(stateService.current.get(_).map(_.lang)).getOrElse(Lang.Default)
  }

  def from(message: Message): Option[Int] = message.from.map(_.id)

  override def receiveMessage(msg: Message): Unit = {
    super.receiveMessage(msg)
  }

  override def receiveCallbackQuery(callbackQuery: CallbackQuery): Unit = {
    super.receiveCallbackQuery(callbackQuery)
  }

  override def run(): Unit = {
    super.run()

    println("Bot has been started")
  }
}
