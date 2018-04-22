package me.slotbook.client.telegram.service

import me.slotbook.client.telegram.model.State

case class StateService(var current: State = State()) {
  def update(state: State): Unit = {
    this.current = state
  }
}
