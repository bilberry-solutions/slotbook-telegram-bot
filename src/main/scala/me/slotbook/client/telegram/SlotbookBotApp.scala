package me.slotbook.client.telegram

object SlotbookBotApp extends App {
  override def main(args: Array[String]): Unit = {
    new SlotbookBot("542319172:AAE0pSkdyCTG501oj2-j8S_0m_AVWwTDz0I").run()
  }
}
