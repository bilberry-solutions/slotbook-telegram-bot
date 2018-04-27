package me.slotbook.client.telegram.model

import com.osinka.i18n.{Lang, Messages}
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.model.Buttons.menuButton
import me.slotbook.client.telegram.model.slotbook._

sealed trait Reply {
  def buttonsFor(collection: Seq[(String, String)])(tagger: String => String): Seq[InlineKeyboardButton] = {
    collection.map(element => InlineKeyboardButton.callbackData(element._2, tagger.apply(element._1)))
  }

  def message: String

  def markup: Option[ReplyMarkup] = None
}

object Icons {
  val searchIcon = "\uD83D\uDD0E"
  val languageIcon = "\uD83C\uDF10"
  val helpIcon = "\uD83D\uDCE2"
  val resetIcon = "❌"
  val menuIcon = "\uD83D\uDCDD"
}

object Tags {
  val CATEGORY_TAG = "category_"
  val SERVICE_TAG = "service_"
  val COMPANY_TAG = "company_"
  val EMPLOYEE_TAG = "employee_"
  val SLOT_TAG = "slot_"
  val DATE_TAG = "date_"
  val MENU_TAG = "help_"
  val LANGUAGE_TAG = "lang_"
}

case class I18nMessage(icon: String, i18n: String) {
  def localizedMessage(lang: Lang): String = icon + " " + Messages(i18n)(lang)
}

object Buttons {

  import me.slotbook.client.telegram.model.Icons._

  def menuButton(prefixTagger: String => String): InlineKeyboardButton =
    InlineKeyboardButton.callbackData(s"$menuIcon Menu", prefixTagger(Tags.MENU_TAG))
}

case class HelpReply() extends Reply {
  override def message: String = "Help information here"
}

case class AskForMenuAction(prefixTagger: String => String, implicit val lang: Lang) extends Reply {

  import me.slotbook.client.telegram.model.AskForMenuAction._
  import me.slotbook.client.telegram.model.Icons._

  val messages = Map(
    START_SEARCH_ACTION_ID -> I18nMessage(searchIcon, "menu.search"),
    CHANGE_LANG_ID -> I18nMessage(languageIcon, "menu.change.language"),
    HELP_ACTION_ID -> I18nMessage(helpIcon, "menu.help"),
    RESET_SEARCH_ACTION_ID -> I18nMessage(resetIcon, "menu.reset.search"))

  override def message: String = {
    messages.mkString("\n")
  }

  override def markup: Option[ReplyMarkup] = {
    val msg = messages.toSeq.map(m => (m._1.toString, m._2.localizedMessage(lang)))
    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(msg)(prefixTagger)))
  }
}

object AskForMenuAction {
  val START_SEARCH_ACTION_ID: Int = 0
  val CHANGE_LANG_ID: Int = 1
  val HELP_ACTION_ID: Int = 2
  val RESET_SEARCH_ACTION_ID: Int = 3
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"

  override def markup: Option[ReplyMarkup] = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(message)))
}

case class AskForNewLanguage(prefixTagger: String => String) extends Reply {
  val messages: Map[String, String] = Map("en" -> "en", "ru" -> "ru", "ua" -> "ua")

  override def message: String = "please.select.your.language"

  override def markup: Option[ReplyMarkup] = {
    val items = messages.toSeq.map(message => (message._1, message._2))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(items)(prefixTagger)))
  }

}

case class AskForServiceCategory(categories: Seq[Service],
                                 prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose service category"

  override def markup: Option[ReplyMarkup] = {
    val ctgs = categories.map(c => (c.id.toString, c.name.toString))
    val buttons = buttonsFor(ctgs)(prefixTagger) :+ menuButton(prefixTagger)

    Some(InlineKeyboardMarkup.singleColumn(buttons))
  }
}

case class AskForClientService(services: Seq[ServiceWithCompaniesCount],
                               prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose service"

  override def markup: Option[ReplyMarkup] = {
    val servs = services.map(c => (c.service.id.toString, s"${c.service.name}"))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(servs)(prefixTagger)))
  }
}

case class AskForCompany(companies: Seq[CompanyDistanceRating],
                         prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose company"

  override def markup: Option[ReplyMarkup] = {
    val comps = companies.map(c => (c.company.id.toString, c.company.name))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForEmployee(employees: Seq[UserWithRating],
                          prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose employee"

  override def markup: Option[ReplyMarkup] = {
    val comps = employees.map(e => (e.user.id, s"${e.user.firstName} ${e.user.lastName}"))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForDates(dates: Seq[(String, String)], prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose a date"

  override def markup: Option[ReplyMarkup] = {
    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(dates)(prefixTagger)))
  }
}

case class AskForSlot(periods: Seq[Period], prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose a timeslot"

  override def markup: Option[ReplyMarkup] = {
    val data = periods.map(p => (p.period.startTime.toString, p.period.startTime.toString))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(data)(prefixTagger)))
  }
}

case class EventCreated() extends Reply {
  override def message: String = "Event has been created"
}

object Errors {
  case class NoSlots() extends Reply {
    override def message: String = "There are no free slots on this date"
  }
}