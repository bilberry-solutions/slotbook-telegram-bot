package me.slotbook.client.telegram

import com.typesafe.config.ConfigFactory

object SlotbookBotApp extends App {
  override def main(args: Array[String]): Unit = {
    val token: String = ConfigFactory.load().getString("auth.telegram.token.value")

    new SlotbookBot(token).run()
  }
}
