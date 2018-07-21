package me.slotbook.client.telegram.model.slotbook

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{Format, Json}

case class Period(period: Timeslot)

case class Timeslot(startDate: String, endDate: String, startTime: String, endTime: String)

case class DateWithTimeslot(date: String, periods: Seq[Period])

case class PeriodWithUser(period: Period, userWithRating: UserWithRating)

object Timeslot {
  type Time = String

  val dateTimeFormatPattern = "dd-MM-yyyy-HH-mm"
  val dateFormatPattern = "dd-MM-yyyy"

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatPattern)
  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatPattern)

  implicit val format: Format[Timeslot] = Json.format[Timeslot]
}

object DateWithTimeslot {
  implicit val format: Format[DateWithTimeslot] = Json.format[DateWithTimeslot]
}

object Period {
  implicit val format: Format[Period] = Json.format[Period]
}

object PeriodWithUser {
  implicit val format: Format[PeriodWithUser] = Json.format[PeriodWithUser]
}