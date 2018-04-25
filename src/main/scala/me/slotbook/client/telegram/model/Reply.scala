package me.slotbook.client.telegram.model

import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.model.slotbook._

sealed trait Reply {
  val searchIcon = "\uD83D\uDD0E"
  val languageIcon = "\uD83C\uDF10"
  val helpIcon = "\uD83D\uDCE2"
  val resetIcon = "❌"

  def buttonsFor(collection: Seq[(String, String)])(tagger: String => String): Seq[InlineKeyboardButton] = {
    collection.map(element => InlineKeyboardButton.callbackData(element._2, tagger.apply(element._1)))
  }

  def message: String

  def markup: Option[ReplyMarkup] = None
}

case class HelpReply() extends Reply {
  override def message: String = "Help information here"
}

case class AskForMenuAction(prefixTagger: String => String) extends Reply {
  import me.slotbook.client.telegram.model.AskForMenuAction._

  val messages = Map(
    START_SEARCH_ACTION_ID -> s"$searchIcon Start search",
    CHANGE_LANG_ID -> s"$languageIcon Change language",
    HELP_ACTION_ID -> s"$helpIcon Help",
    RESET_SEARCH_ACTION_ID -> s"$resetIcon Reset search")

  override def message: String = {
    messages.mkString("\n")
  }

  override def markup: Option[ReplyMarkup] = {
    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(messages.toSeq.map(m => (m._1.toString, m._2)))(prefixTagger)))
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

case class AskForNewLanguage() extends Reply {
  override def message: String = "Please select your language"

  override def markup: Option[ReplyMarkup] = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.text("language")))

}

case class AskForServiceCategory(categories: Seq[Service],
                                 prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose service category"

  override def markup: Option[ReplyMarkup] = {
    val ctgs = categories.map(c => (c.id.toString, c.name.toString))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(ctgs)(prefixTagger)))
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