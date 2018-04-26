package me.slotbook.client.telegram.model

import com.osinka.i18n.Lang
import me.slotbook.client.telegram.model.slotbook._

case class State(userId: Option[Int] = None,
                 categoryId: Option[Service.ID] = None,
                 serviceId: Option[Service.ID] = None,
                 companyId: Option[Company.ID] = None,
                 employeeId: Option[User.ID] = None,
                 location: Option[Location] = None,
                 slotId: Option[String] = None,
                 lang: Lang = Language.default)
