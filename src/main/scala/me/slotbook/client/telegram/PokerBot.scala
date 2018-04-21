package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.{Extractors, Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.methods.{EditMessageReplyMarkup, SendGame}
import info.mukel.telegrambot4s.models.InlineKeyboardButton.callbackData
import info.mukel.telegrambot4s.models.InlineKeyboardMarkup.singleButton
import info.mukel.telegrambot4s.models._

import scala.concurrent.Future
import scala.util.Random

object A extends App {
  override def main(args: Array[String]): Unit = {
    new PokerBot("542319172:AAE0pSkdyCTG501oj2-j8S_0m_AVWwTDz0I").run()
  }
}

class PokerBot(val token: String) extends TelegramBot with Polling with Commands with Callbacks {

  val TAG = "COUNTER_TAG"
  var requestCount = 0

  def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    singleButton(callbackData(s"Press me!!!\n$n - $requestCount", prefixTag(TAG)(n.toString)))
  }

  onCommand("/counter") { implicit msg =>
    reply("Press to increment!", replyMarkup = Some(markupCounter(0)))
  }

  onCallbackQuery { implicit cbq => println(cbq) }

  onCallbackWithTag(TAG) { implicit cbq =>
    // Notification only shown to the user who pressed the button.
    ackCallback(Some(cbq.from.firstName + " pressed the button!"))
    // Or just ackCallback()

    for {
      data <- cbq.data
      Extractors.Int(n) = data
      msg <- cbq.message
    } /* do */ {
      request(
        EditMessageReplyMarkup(
          Some(ChatId(msg.source)), // msg.chat.id
          Some(msg.messageId),
          replyMarkup = Some(markupCounter(n + 1))))
    }
  }

  override def run(): Unit = {
    super.run()

    println("Bot has been started")
  }
}
