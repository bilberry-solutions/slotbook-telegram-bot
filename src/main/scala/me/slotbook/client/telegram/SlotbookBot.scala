package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.models.KeyboardButton.requestLocation
import info.mukel.telegrambot4s.models.ReplyKeyboardMarkup.singleButton
import me.slotbook.client.telegram.dao.{InMemoryStateDao, StateDao}
import me.slotbook.client.telegram.model.AskForClientLocation
import me.slotbook.client.telegram.service.SlotbookApiClient

import scala.concurrent.Future

class SlotbookBot(tok: String) extends TelegramBot with Polling with Commands with Callbacks {
    override def token: String = tok

    val slotbookApiClient: SlotbookApiClient = new SlotbookApiClient()
    val stateDao: StateDao = new InMemoryStateDao()

    onCommand('help) { implicit msg =>
      reply("Just use /find")
    }

    onCommand('users) { implicit msg =>
      stateDao.getAll.map { userIds =>
        reply(userIds.mkString(","))
      }
    }

    onCommand('find) { implicit msg =>
      println(msg.from)

      msg.from match {
        case Some(user) => stateDao.registerUser(user)
        case None => Future.failed(new RuntimeException("Unable to register anonymous user"))
      }

      reply(
        text = AskForClientLocation().message,
        replyMarkup = Some(singleButton(requestLocation(AskForClientLocation().message)))).map { message =>
        println(message)
      }
    }

    onCallbackQuery { implicit callbackQuery =>
      println(callbackQuery)
      println(s"gameShortName: ${callbackQuery.gameShortName}")
      println(s"data: ${callbackQuery.data}")

      callbackQuery.data.foreach { data =>
        ackCallback(text = Some("Hello Pepe"), url = Some("https://t.me/MenialBot?start=1234"))
      }

      // You must acknowledge callback queries, even if there's no response.
      // e.g. just ackCallback()

      // To open game, you may need to pass extra (url-encoded) information to the game.
      //ackCallback(url = Some("https://my.awesome.game.com/awesome"))
    }

    onCommand('contact) { implicit msg =>
      println(msg)
      reply("Location has been accepted")
    }

    onCommand('category) { implicit msg =>
      slotbookApiClient.listCategories.map { categories =>
        reply(categories.mkString("\n"))
      }
    }

    override def run(): Unit = {
      super.run()

      println("Bot has been started")
    }
}
