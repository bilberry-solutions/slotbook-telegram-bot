package me.slotbook.client.telegram.model

import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup, ReplyMarkup}
import me.slotbook.client.telegram.service.SlotbookApiClient

sealed trait Reply {
  def message: String

  def markup: Option[ReplyMarkup] = None
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"
}

case class AskForServiceCategory(categories: Map[SlotbookApiClient#ServiceId, SlotbookApiClient#ServiceName],
                                 prefixTagger: SlotbookApiClient#ServiceName => String) extends Reply {
  override def message: String = "Please choose service category"

  override def markup: Option[ReplyMarkup] = {

    val toCategoryButtonMarkup: (SlotbookApiClient#Service => InlineKeyboardButton) = category =>
      InlineKeyboardButton.callbackData(category._2, prefixTagger.apply(category._2))

    val markup = InlineKeyboardMarkup.singleColumn(categories.map(toCategoryButtonMarkup).toSeq)

    Some(markup)
  }
}

case class AskForClientService(services: Map[SlotbookApiClient#ServiceId, SlotbookApiClient#ServiceName],
                               prefixTagger: SlotbookApiClient#ServiceName => String) extends Reply {
  override def message: String = "Please choose a service"

  override def markup: Option[ReplyMarkup] = {

    val toServiceButtonMarkup: (SlotbookApiClient#Service => InlineKeyboardButton) = category =>
      InlineKeyboardButton.callbackData(category._2, prefixTagger.apply(category._2))

    val markup = InlineKeyboardMarkup.singleColumn(services.map(toServiceButtonMarkup).toSeq)

    Some(markup)
  }
}