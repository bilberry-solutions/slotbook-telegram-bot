package me.slotbook.client.telegram.service

import java.util.concurrent.ConcurrentHashMap

import com.osinka.i18n.Lang
import me.slotbook.client.telegram.model.State
import me.slotbook.client.telegram.model.slotbook.{LatLng, Timeslot}
import org.joda.time.LocalDate

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future

case class StateService(current: mutable.Map[Int, State] = new ConcurrentHashMap[Int, State]().asScala) {
  def updateLanguage(userId: Int, lang: Lang): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(lang = lang))
  }

  def updateLocation(userId: Int, location: Option[LatLng]): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(location = location))
  }

  def updateCategory(userId: Int, categoryId: Int): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(categoryId = Some(categoryId)))
  }

  def updateService(userId: Int, serviceId: Int): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(serviceId = Some(serviceId)))
  }

  def updateCompany(userId: Int, companyId: Int): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(companyId = Some(companyId)))
  }

  def updateEmployee(userId: Int, employeeId: String): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(employeeId = Some(employeeId)))
  }

  def updateSlotDate(userId: Int, date: LocalDate): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(slotDate = Some(date)))
  }

  def updateSlotTimes(userId: Int, times: Timeslot.Times): Unit = {
    this.update(userId, this.current.getOrElse(userId, State()).copy(slotTimes = Some(times)))
  }

  private def update(userId: Int, state: State): Unit = {
    this.current.update(userId, state)
  }

  def reset(userId: Int): Future[Unit] = {
    Future.successful(this.current.update(userId, State()))
  }
}
