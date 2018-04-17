package me.slotbook.client.telegram.dao

import info.mukel.telegrambot4s.models.User

import scala.concurrent.Future

sealed trait State {

  object LOCATION_PROVIDED extends State

  object SERVICE_CATEGORY_PROVIDED extends State

  object SERVICE_PROVIDED extends State

}

trait StateDao {
  type USER_ID = Int

  def registerUser(user: User): Future[USER_ID]

  def getAll: Future[Seq[USER_ID]]
}

class InMemoryStateDao extends StateDao {
  var state: Map[USER_ID, String] = Map()

  override def registerUser(user: User): Future[USER_ID] = {
    state = state.+(user.id -> "find")

    Future.successful(user.id)
  }

  override def getAll: Future[Seq[USER_ID]] = Future.successful(state.keys.toSeq)
}


