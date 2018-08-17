package me.slotbook.client.telegram

object SlotbookBotApp extends App {
  override def main(args: Array[String]): Unit = {
    new SlotbookBot("").run()
  }
}
