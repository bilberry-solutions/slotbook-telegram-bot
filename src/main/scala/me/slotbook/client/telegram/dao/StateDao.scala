package me.slotbook.client.telegram.dao

import info.mukel.telegrambot4s.models.{Contact, User}

import scala.concurrent.Future

trait StateDao {
  type USER_ID = Int

  def registerUser(user: User): Future[USER_ID]
}

class InMemoryStateDao extends StateDao {
  val state: Map[USER_ID, String] = Map()

  override def registerUser(user: User): Future[USER_ID] = {
    state.+(user.id -> "find")

    Future.successful(user.id)
  }
}


