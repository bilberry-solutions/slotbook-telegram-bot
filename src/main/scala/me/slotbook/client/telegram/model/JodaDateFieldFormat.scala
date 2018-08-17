package me.slotbook.client.telegram.model

import org.joda.time.{DateTime, LocalDate, LocalTime}
import play.api.libs.json._
import Constants._

trait JodaDateFieldFormat {
    implicit val dateReads: Reads[LocalDate] = Reads[LocalDate](json =>
        json.validate[String].map(date => dateFormatter.parseLocalDate(date))
    )

    implicit val dateWrites: Writes[LocalDate] = (date: LocalDate) => JsString(date.toString(dateFormatter))

    implicit val timeReads: Reads[LocalTime] = (json: JsValue) => json.validate[String].map(time => timeFormatter.parseLocalTime(time))

    implicit val timeWrites: Writes[LocalTime] = (time: LocalTime) => JsString(time.toString(timeFormatter))

    implicit val dateTimeReads: Reads[DateTime] = Reads[DateTime](json =>
        json.validate[String].map(date => DateTime.parse(date, dateTimeFormatter))
    )

    implicit val dateTimeWrites: Writes[DateTime] = (date: DateTime) => JsString(dateTimeFormatter.print(date))
}
