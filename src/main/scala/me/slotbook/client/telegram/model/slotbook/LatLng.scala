package me.slotbook.client.telegram.model.slotbook

import me.slotbook.client.telegram.model.slotbook.LatLng.{Lat, Lng}

case class LatLng(lat: Lat, lng: Lng)

object LatLng {
  type Lat = BigDecimal

  type Lng = BigDecimal
}
