package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.Commands
import info.mukel.telegrambot4s.Implicits._
import info.mukel.telegrambot4s.models.{KeyboardButton, ReplyKeyboardMarkup}
import me.slotbook.client.telegram.dao.{InMemoryStateDao, StateDao}

object SlotbookBotApp extends App {
  override def main(args: Array[String]): Unit = {

  }
}

sealed trait Reply {
  def message: String
}

case class AskForClientLocation() extends Reply {
  override def message: String = "Please send me your location"
}

class Bot(tok: String) extends TelegramBot with Polling with Commands {
  override def token: String = tok

  val stateDao: StateDao = new InMemoryStateDao()

  onCommand('help) { implicit msg =>
    reply("Just use /find")
  }

  onCommand('find) { implicit msg =>
    println(msg.from)

    stateDao.registerUser(msg.contact.get)

    reply(
      text = AskForClientLocation().message,
      replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(AskForClientLocation().message))))
  }

  override def run(): Unit = {
    super.run()

    println("Bot has been started")
  }
}
