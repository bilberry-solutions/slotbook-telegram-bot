package me.slotbook.client.telegram.service

import me.slotbook.client.telegram.model.State

import scala.concurrent.Future

case class StateService(var current: State = State()) {
  def update(state: State): Unit = {
    this.current = state
  }

  def reset(): Future[Unit] = {
    this.current = State()

    Future.successful()
  }
}
