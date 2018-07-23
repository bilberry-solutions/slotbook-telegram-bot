package me.slotbook.client.telegram.model.slotbook

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{DateTime, LocalDate, LocalTime}

object Constants {
  val timeFormatPattern = "HH:mm"
  val dateFormatPattern = "dd-MM-yyyy"
  val dateTimeFormatPattern = "dd-MM-yyyy-HH-mm"
  val uiDateTimeFormatPattern = "dd-MM-yyyy HH:mm"
  val uiDateFormatPattern = "yyyy-MM-dd"
  val yearFormatPattern = "yyyy"

  val dateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateFormatPattern)
  val uiDateFormatter: DateTimeFormatter = DateTimeFormat.forPattern(uiDateFormatPattern)
  val uiDateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern(uiDateTimeFormatPattern)
  val timeFormatter: DateTimeFormatter = DateTimeFormat.forPattern(timeFormatPattern)
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern(dateTimeFormatPattern)
  val yearFormatter: DateTimeFormatter = DateTimeFormat.forPattern(yearFormatPattern)

  val _00_00: LocalTime = timeFormatter.parseLocalTime("00:00")
  val _1970: LocalDate = new DateTime(1970, 1, 1, 0, 0, 0).toLocalDate
  val _2099: LocalDate = new DateTime(2099, 1, 1, 0, 0, 0).toLocalDate
  val MAX_DATE: LocalDate = new DateTime(9999, 1, 1, 0, 0, 0).toLocalDate
}
