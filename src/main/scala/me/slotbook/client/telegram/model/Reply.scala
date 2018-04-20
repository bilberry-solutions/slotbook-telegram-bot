package me.slotbook.client.telegram.model

import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup, ReplyMarkup}
import me.slotbook.client.telegram.service.SlotbookApiClient

sealed trait Reply {
  def buttonsFor(collection: Seq[(String, String)])(tagger: String => String): Seq[InlineKeyboardButton] = {
    collection.map(element => InlineKeyboardButton.callbackData(element._1, tagger.apply(element._1)))
  }

  def message: String

  def markup: Option[ReplyMarkup] = None
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"
}

case class AskForServiceCategory(categories: Map[SlotbookApiClient#ServiceId, SlotbookApiClient#ServiceName],
                                 prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose service category"

  override def markup: Option[ReplyMarkup] = {
    val ctgs = categories.map(c => (c._1.toString, c._2.toString)).toSeq

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(ctgs)(prefixTagger)))
  }
}

case class AskForClientService(services: Map[SlotbookApiClient#ServiceId, SlotbookApiClient#ServiceName],
                               prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose service"

  override def markup: Option[ReplyMarkup] = {
    val servs = services.map(c => (c._1.toString, c._2.toString)).toSeq

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(servs)(prefixTagger)))
  }
}

case class AskForCompany(companies: Seq[(SlotbookApiClient#Company)],
                         prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose company"

  override def markup: Option[ReplyMarkup] = {
    val comps = companies.map(c => (c._1.toString, c._2.toString))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForEmployee(employees: Seq[(SlotbookApiClient#Employee)],
                          prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose employee"

  override def markup: Option[ReplyMarkup] = {
    val comps = employees.map(e => (e._1, e._2.toString))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(comps)(prefixTagger)))
  }
}

case class AskForSlot(slots: Seq[SlotbookApiClient#Timeslot], prefixTagger: String => String) extends Reply {
  override def message: String = "Please choose a timeslot"

  override def markup: Option[ReplyMarkup] = {
    val data = slots.map(e => (e._1.toString, e._2))

    Some(InlineKeyboardMarkup.singleColumn(buttonsFor(data)(prefixTagger)))
  }
}