package me.slotbook.client.telegram

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.models._
import me.slotbook.client.telegram.dao.{InMemoryStateDao, StateDao}
import me.slotbook.client.telegram.model.{AskForClientLocation, AskForClientService, AskForServiceCategory}
import me.slotbook.client.telegram.service.DefaultSlotbookApiClient

import scala.concurrent.Future

class SlotbookBot(tok: String) extends TelegramBot with Polling with Commands with Callbacks {
  override def token: String = tok

  val CATEGORY_TAG = "category"
  val SERVICE_TAG = "service"

  val slotbookApiClient: DefaultSlotbookApiClient = new DefaultSlotbookApiClient()
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
      replyMarkup = Some(ReplyKeyboardMarkup.singleButton(KeyboardButton.requestLocation(AskForClientLocation().message)))).map { message =>
      println(message)
    }
  }

  onCommand('category) { implicit msg =>
    slotbookApiClient.listCategories.map { categories =>
      val msf = AskForServiceCategory(categories, prefixTag(CATEGORY_TAG))
      reply(msf.message, replyMarkup = msf.markup)
    }
  }

  onMessage { implicit msg =>
    if (msg.location.isDefined) {
      println(s"User's location: ${msg.location}")

      // update user's location
    }
  }

  onCallbackWithTag(CATEGORY_TAG) { implicit callback =>
    println(s"category: $callback")

    ackCallback(text = Some("Category has been accepted"))

    callback.message.map { message =>
      slotbookApiClient.listCategoryServices.map { services =>
        val rpl = AskForClientService(services, prefixTag(SERVICE_TAG))

        reply(rpl.message, replyMarkup = rpl.markup)(message)
      }
    }
  }

  onCallbackWithTag(SERVICE_TAG) { implicit callback =>
    println(s"category: $callback")

    ackCallback(text = Some("Service has been accepted"))

    callback.message.map { message =>
      reply("Service has been accepted")(message)
    }
  }

  override def receiveMessage(msg: Message): Unit = {
    super.receiveMessage(msg)

    println(msg)
  }

  override def receiveCallbackQuery(callbackQuery: CallbackQuery): Unit

  = {
    super.receiveCallbackQuery(callbackQuery)

    println(callbackQuery)
  }

  override def run(): Unit

  = {
    super.run()

    println("Bot has been started")
  }
}
