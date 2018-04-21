package me.slotbook.client.telegram.model

import me.slotbook.client.telegram.model.slotbook._

case class State(categoryId: Service.ID, serviceId: Service.ID, companyId: Company.ID, employeeId: User.ID,
                 location: Location, slot: Timeslot)
