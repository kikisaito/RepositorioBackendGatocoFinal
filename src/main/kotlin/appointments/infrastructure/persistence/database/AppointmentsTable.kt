package com.example.appointments.infrastructure.persistence.database

import com.example.auth.infrastructure.persistence.database.ClientsTable
import com.example.patients.infrastructure.persistence.database.PatientsTable
import com.example.services.infrastructure.persistence.database.ServiceTypesTable
import com.example.auth.infrastructure.persistence.database.VeterinariansTable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.kotlin.datetime.time

/**
 * AppointmentsTable
 * Definición de la tabla appointments en Exposed
 */
object AppointmentsTable : IntIdTable("appointments") {
    val clientId = integer("client_id").references(ClientsTable.id)
    val patientId = integer("patient_id").references(PatientsTable.id)
    val serviceTypeId = integer("service_type_id").references(ServiceTypesTable.id)
    val veterinarianId = integer("veterinarian_id").references(VeterinariansTable.id)
    val date = date("date")
    val time = time("time")
    val status = varchar("status", 20).default("pendiente")
    
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}












