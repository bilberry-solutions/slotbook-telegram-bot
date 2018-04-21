package me.slotbook.client.telegram.model.slotbook

import me.slotbook.client.telegram.model.slotbook.Location.{Lat, Lng}

case class Location(lat: Lat, lng: Lng)

object Location {
  type Lat = BigDecimal

  type Lng = BigDecimal
}
