package me.slotbook.client.telegram.model

import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup, ReplyMarkup}
import me.slotbook.client.telegram.model.slotbook._

sealed trait Reply {
  def buttonsFor(collection: Seq[(String, String)])(tagger: String => String): Seq[InlineKeyboardButton] = {
    collection.map(element => InlineKeyboardButton.callbackData(element._2, tagger.apply(element._1)))
  }

  def message: String

  def markup: Option[ReplyMarkup] = None
}

case class AskForMenuAction(prefixTagger: String => String) extends Reply {
  val messages = Seq(1 -> "Change language", 2 -> "Help", 3 -> "Reset search")

  override def message: String = {
    messages.mkString("\n")
  }

  override def markup: Option[ReplyMarkup] = {
    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(messages.map(m => (m._1.toString, m._2)))(prefixTagger)))
  }
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"
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