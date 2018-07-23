package me.slotbook.client.telegram.model.slotbook

import org.joda.time.LocalTime.MIDNIGHT
import org.joda.time.{LocalDate, LocalTime}
import play.api.libs.functional.syntax._
import play.api.libs.json._

/*
 * The names of the fields in classes should be changed only if corresponding names on slotbook model were changed.
 */
case class Event(startDate: LocalDate = Constants._1970,
                 endDate: LocalDate = Constants.MAX_DATE,
                 startTime: LocalTime = MIDNIGHT,
                 endTime: LocalTime = MIDNIGHT) {
  def toJson: JsValue = Event.eventWrites.writes(this)
}

object Event extends JodaDateFieldFormat {
  implicit val eventReads: Reads[Event] = (
    (JsPath \ "startDate").read[LocalDate](dateReads) and
      (JsPath \ "endDate").read[LocalDate](dateReads) and
      (JsPath \ "startTime").read[LocalTime](timeReads) and
      (JsPath \ "endTime").read[LocalTime](timeReads)
    ) (Event.apply _)

  implicit val eventWrites: Writes[Event] = (
    (JsPath \ "startDate").write[LocalDate](dateWrites) and
      (JsPath \ "endDate").write[LocalDate](dateWrites) and
      (JsPath \ "startTime").write[LocalTime](timeWrites) and
      (JsPath \ "endTime").write[LocalTime](timeWrites)
    ) (unlift(Event.unapply))
}