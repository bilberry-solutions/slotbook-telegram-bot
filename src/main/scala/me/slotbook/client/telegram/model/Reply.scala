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

case class I18nMessage(icon: Option[String] = None, i18n: String) {
  def localizedMessage(lang: Lang): String = icon.getOrElse("") + " " + Messages(i18n)(lang)
}

object Buttons {

  import me.slotbook.client.telegram.model.Icons._

  def menuButton(prefixTagger: String => String)(implicit lang: Lang): InlineKeyboardButton =
    InlineKeyboardButton.callbackData(I18nMessage(Some(menuIcon), "menu").localizedMessage(lang), prefixTagger(Tags.MENU_TAG))
}

case class HelpReply(lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "help").localizedMessage(lang)
}

case class AskForMenuAction(prefixTagger: String => String, implicit val lang: Lang) extends Reply {

  import me.slotbook.client.telegram.model.AskForMenuAction._
  import me.slotbook.client.telegram.model.Icons._

  val messages = Map(
    START_SEARCH_ACTION_ID -> I18nMessage(Some(searchIcon), "menu.search"),
    CHANGE_LANG_ID -> I18nMessage(Some(languageIcon), "menu.change.language"),
    HELP_ACTION_ID -> I18nMessage(Some(helpIcon), "menu.help"),
    RESET_SEARCH_ACTION_ID -> I18nMessage(Some(resetIcon), "menu.reset.search"))

  override def message: String = {
    I18nMessage(i18n = "ask.for.menu.item").localizedMessage(lang)
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

case class AskForClientLocation(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.client.location").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(message)))
}

case class AskForNewLanguage(prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  val messages: Map[String, String] = Map("en" -> "en", "ru" -> "ru", "ua" -> "ua")

  override def message: String = I18nMessage(i18n = "ask.for.client.language").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val items = messages.toSeq.map(message => (message._1, message._2))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(items)(prefixTagger)))
  }
}

case class AskForServiceCategory(categories: Seq[Service],
                                 prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.service.category").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val ctgs = categories.map(c => (c.id.toString, c.name.toString))
    val buttons = buttonsFor(ctgs)(prefixTagger) :+ menuButton(prefixTagger)

    Some(InlineKeyboardMarkup.singleColumn(buttons))
  }
}

case class AskForClientService(services: Seq[ServiceWithCompaniesCount],
                               prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.service").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val servs = services.map(c => (c.service.id.toString, s"${c.service.name}"))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(servs)(prefixTagger)))
  }
}

case class AskForCompany(companies: Seq[CompanyDistanceRating],
                         prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.company").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val comps = companies.map(c => (c.company.id.toString, c.company.name))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForEmployee(employees: Seq[UserWithRating],
                          prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.employee").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val comps = employees.map(e => (e.user.id, s"${e.user.firstName} ${e.user.lastName}"))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForDates(dates: Seq[(String, String)], prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.date").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(dates)(prefixTagger)))
  }
}

case class AskForSlot(periods: Seq[Period], prefixTagger: String => String)(implicit lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "ask.for.time").localizedMessage(lang)

  override def markup: Option[ReplyMarkup] = {
    val data = periods.map(p => (p.period.startTime.toString, p.period.startTime.toString))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(data)(prefixTagger)))
  }
}

case class EventCreated(lang: Lang) extends Reply {
  override def message: String = I18nMessage(i18n = "event.created").localizedMessage(lang)
}

object Errors {

  case class NoSlots(lang: Lang) extends Reply {
    override def message: String = I18nMessage(i18n = "no.free.slots").localizedMessage(lang)
  }

}