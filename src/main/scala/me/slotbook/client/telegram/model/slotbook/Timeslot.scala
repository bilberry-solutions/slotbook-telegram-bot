package me.slotbook.client.telegram.model.slotbook

import org.joda.time.LocalTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import play.api.libs.json.{Format, Json}

case class DateWithTimeslot(date: String, periods: Seq[Timeslot])

case class PeriodWithUser(period: Event, userWithRating: UserWithRating)

case class Timeslot(period: Event)

object Timeslot {
  type Times = (LocalTime, LocalTime)

  val dateTimeFormatPattern = "dd-MM-yyyy-HH-mm"
  val dateFormatPattern = "dd-MM-yyyy"

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatPattern)
  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatPattern)

  implicit val format: Format[Timeslot] = Json.format[Timeslot]

  def printTimeslot(times: Times): String = s"${Constants.timeFormatter.print(times._1)}-${Constants.timeFormatter.print(times._2)}"

  def parseTimeslot(timeslot: String): Times = {
    val times = timeslot.split("-")

    (Constants.timeFormatter.parseLocalTime(times(0)), Constants.timeFormatter.parseLocalTime(times(1)))
  }
}

object DateWithTimeslot {
  implicit val format: Format[DateWithTimeslot] = Json.format[DateWithTimeslot]
}

object PeriodWithUser {
  implicit val format: Format[PeriodWithUser] = Json.format[PeriodWithUser]
}