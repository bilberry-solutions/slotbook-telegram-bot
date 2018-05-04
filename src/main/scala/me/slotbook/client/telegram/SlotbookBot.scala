package me.slotbook.client.telegram

import com.osinka.i18n.{Lang, Messages}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods.EditMessageReplyMarkup
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.model.Tags._
import me.slotbook.client.telegram.model._
import me.slotbook.client.telegram.model.slotbook.Timeslot.{dateFormatter, dateTimeFormatter}
import me.slotbook.client.telegram.model.slotbook.{Language, Location}
import me.slotbook.client.telegram.service.{DefaultSlotbookApiClient, StateService}
import org.joda.time.LocalDate

import scala.concurrent.Future

class SlotbookBot(val token: String) extends TelegramBot with Polling with Commands with Callbacks {

  val slotbookApiClient: DefaultSlotbookApiClient = new DefaultSlotbookApiClient()
  val stateService = StateService()

  onCommand('menu) { implicit msg =>
    replyWithNew(AskForMenuAction(prefixTag(MENU_TAG), languageOf(msg)), msg)
  }

  onMessage { implicit message =>
    if (message.location.isDefined && message.from.isDefined) {
      val loc = message.location.map(loc => Location(BigDecimal(loc.latitude), BigDecimal(loc.longitude)))
      val language = languageOf(message)
      val state = stateOf(message.chat.id.toInt)

      stateService.updateLocation(message.chat.id.toInt, loc)

      /*slotbookApiClient.listCategories(language).map { categories =>
        replyWithNew(AskForServiceCategory(categories, prefixTag(CATEGORY_TAG))(language), message)
      }*/
      state match {
        case Some(currentState) if currentState.serviceId.isDefined =>
          slotbookApiClient.listCompaniesByService(currentState.serviceId.get, loc)(language).map { companies =>
            replyWithNew(AskForCompany(companies, prefixTag(COMPANY_TAG))(language), message)
          }
        case None => println("Unable to extract current state or service was not selected")
      }
    }
  }

  onCallbackWithTag(MENU_TAG) { implicit callback =>
    val language = languageOf(callback)

    ackCallback(text = Some(Messages("accepted.please.wait")(language)))

    callback.message match {
      case Some(message) if message.from.isDefined =>
        callback.data.map(_.toInt) match {
          case Some(AskForMenuAction.START_SEARCH_ACTION_ID) =>
            slotbookApiClient.listCategories(language).map { categories =>
              replyWithNew(AskForServiceCategory(categories, prefixTag(CATEGORY_TAG))(language), message)
            }
          case Some(AskForMenuAction.CHANGE_LANG_ID) =>
            replyOverriding(AskForNewLanguage(prefixTag(LANGUAGE_TAG))(language), message)
          case Some(AskForMenuAction.HELP_ACTION_ID) =>
            replyWithNew(HelpReply(language), message)
          case Some(AskForMenuAction.RESET_SEARCH_ACTION_ID) =>
            stateService.reset(callback.from.id).map(_ =>
              replyWithNew(AskForMenuAction(prefixTag(MENU_TAG), language), message))
        }
      case None => println("Unable to define <from> of the message")
    }
  }

  onCallbackWithTag(LANGUAGE_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.message match {
      case Some(message) if message.from.isDefined =>
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
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(categoryId) =>
        callback.message match {
          case Some(message) if message.from.isDefined =>
            val language = languageOf(callback)

            stateService.updateCategory(callback.from.id, categoryId.toInt)
            slotbookApiClient.listCategoryServices(categoryId.toInt)(language).map { services =>
              replyWithNew(AskForClientService(services, prefixTag(SERVICE_TAG))(language), message)
            }
          case None => println("Unable to extract message from callback")
        }

      case None => println("Category was not selected")
    }
  }

  /* Handling service selection */
  onCallbackWithTag(SERVICE_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(serviceId) =>
        stateService.updateService(callback.from.id, serviceId.toInt)
        callback.message match {
          case Some(message) if message.from.isDefined =>
            val language = languageOf(callback)

            /*slotbookApiClient.listCompaniesByService(serviceId.toInt,
              stateService.current(callback.message.get.from.get.id).location)(language).map { companies =>
              replyWithNew(AskForCompany(companies, prefixTag(COMPANY_TAG))(language), message)
            }*/
            replyWithNew(AskForClientLocation(language), message)
          case None => println("Unable to extract message from callback")
        }
      case None => println("Service was not selected")
    }
  }

  /* Handling company selection */
  onCallbackWithTag(COMPANY_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(companyId) =>
        callback.message match {
          case Some(message) if message.from.isDefined =>
            stateService.updateCompany(callback.from.id, companyId.toInt)

            val language = languageOf(callback)

            slotbookApiClient.listEmployeesByCompany(companyId.toInt)(language).map { employees =>
              replyWithNew(AskForEmployee(employees, prefixTag(EMPLOYEE_TAG))(language), message)
            }
          case None => println("Unable to extract message from callback")
        }
      case None => println("Employee was not selected")
    }
  }

  /* Handling company selection */
  onCallbackWithTag(EMPLOYEE_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(employeeId) =>
        stateService.updateEmployee(callback.from.id, employeeId)

        callback.message match {
          case Some(message) if message.from.isDefined =>
            val dates = 1.to(3)
              .map(LocalDate.now.toDateTimeAtStartOfDay.plusDays(_))
              .map(d => (dateTimeFormatter.print(d), dateFormatter.print(d)))

            // TODO check why api returns slots in the past
            val rpl = AskForDates(dates, prefixTag(DATE_TAG))(languageOf(message))

            replyWithNew(rpl, message)
          case None => println("Unable to extract message from callback")
        }

      case None => println("Date was not selected")
    }
  }

  onCallbackWithTag(DATE_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(date) =>
        callback.message match {
          case Some(message) if message.from.isDefined =>
            val language = languageOf(callback)
            val currentState = stateService.current(callback.from.id)

            if (currentState.serviceId.isDefined && currentState.employeeId.isDefined) {
              slotbookApiClient.listSlots(currentState.serviceId.get, currentState.employeeId.get, date)(language).map { slots =>
                if (slots.nonEmpty) {
                  replyWithNew(AskForSlot(slots, prefixTag(SLOT_TAG))(language), message)
                } else {
                  replyWithNew(Errors.NoSlots(language), message)
                }
              }
            } else {
              reply("Please select a service and employee to register a visit")(message)
            }
          case None => println("Unable to extract message from callback")
        }
      case None => println("Timeslot was not selected")
    }
  }

  onCallbackWithTag(SLOT_TAG) { implicit callback =>
    ackCallback(text = Some(Messages("accepted.please.wait")(languageOf(callback))))

    callback.data match {
      case Some(slotId) =>
        stateService.updateEmployee(callback.from.id, slotId)

        callback.message match {
          case Some(message) if message.from.isDefined =>
            val language = languageOf(callback)

            slotbookApiClient.bindSlot(slotId.toInt, callback.from)(languageOf(message)).map { slots =>
              replyWithNew(EventCreated(language), message)
            }
          case None => println("Unable to extract message from callback")
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

  def languageOf(message: Message): Lang = {
    message.from.map(_.id).flatMap(stateOf(_).map(_.lang)).getOrElse(Lang.Default)
  }

  def languageOf(callback: CallbackQuery): Lang =
    stateOf(callback.from.id).map(_.lang).getOrElse(Lang.Default)

  def stateOf(userId: Int): Option[State] = stateService.current.get(userId)

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
