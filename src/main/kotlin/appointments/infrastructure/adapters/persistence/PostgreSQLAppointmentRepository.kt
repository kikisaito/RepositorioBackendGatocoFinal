package com.example.appointments.infrastructure.adapters.persistence

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository
import com.example.appointments.infrastructure.persistence.database.AppointmentsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.datetime.*

class PostgreSQLAppointmentRepository : AppointmentRepository {
    
    override suspend fun findById(id: Int): Appointment? = transaction {
        AppointmentsTable.selectAll()
            .where { AppointmentsTable.id eq id }
            .singleOrNull()
            ?.let { rowToAppointment(it) }
    }
    
    override suspend fun findByClientId(clientId: Int): List<Appointment> = transaction {
        AppointmentsTable.selectAll()
            .where { AppointmentsTable.clientId eq clientId }
            .orderBy(AppointmentsTable.date to SortOrder.DESC, AppointmentsTable.time to SortOrder.DESC)
            .map { rowToAppointment(it) }
    }
    
    override suspend fun findByPatientId(patientId: Int): List<Appointment> = transaction {
        AppointmentsTable.selectAll()
            .where { AppointmentsTable.patientId eq patientId }
            .orderBy(AppointmentsTable.date to SortOrder.DESC, AppointmentsTable.time to SortOrder.DESC)
            .map { rowToAppointment(it) }
    }
    
    override suspend fun findByVeterinarianId(veterinarianId: Int): List<Appointment> = transaction {
        AppointmentsTable.selectAll()
            .where { AppointmentsTable.veterinarianId eq veterinarianId }
            .orderBy(AppointmentsTable.date to SortOrder.DESC, AppointmentsTable.time to SortOrder.DESC)
            .map { rowToAppointment(it) }
    }
    
    override suspend fun findAll(): List<Appointment> = transaction {
        AppointmentsTable.selectAll()
            .orderBy(AppointmentsTable.date to SortOrder.DESC, AppointmentsTable.time to SortOrder.DESC)
            .map { rowToAppointment(it) }
    }
    
    override suspend fun save(appointment: Appointment): Appointment = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        
        // Convertir java.time.LocalDate a kotlinx.datetime.LocalDate
        val kotlinxLocalDate = kotlinx.datetime.LocalDate(
            appointment.date.year,
            appointment.date.monthValue,
            appointment.date.dayOfMonth
        )
        
        // Convertir java.time.LocalTime a kotlinx.datetime.LocalTime
        val kotlinxLocalTime = kotlinx.datetime.LocalTime(
            appointment.time.hour,
            appointment.time.minute,
            appointment.time.second
        )
        
        val id = AppointmentsTable.insertAndGetId {
            it[clientId] = appointment.clientId
            it[patientId] = appointment.patientId
            it[serviceTypeId] = appointment.serviceTypeId
            it[veterinarianId] = appointment.veterinarianId
            it[date] = kotlinxLocalDate
            it[time] = kotlinxLocalTime
            it[status] = appointment.status
            it[notes] = appointment.notes
            it[createdAt] = nowLocalDateTime
            it[updatedAt] = nowLocalDateTime
        }.value
        
        appointment.copy(id = id, createdAt = now, updatedAt = now)
    }
    
    override suspend fun update(appointment: Appointment): Appointment = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        
        // Convertir java.time.LocalDate a kotlinx.datetime.LocalDate
        val kotlinxLocalDate = kotlinx.datetime.LocalDate(
            appointment.date.year,
            appointment.date.monthValue,
            appointment.date.dayOfMonth
        )
        
        // Convertir java.time.LocalTime a kotlinx.datetime.LocalTime
        val kotlinxLocalTime = kotlinx.datetime.LocalTime(
            appointment.time.hour,
            appointment.time.minute,
            appointment.time.second
        )
        
        AppointmentsTable.update({ AppointmentsTable.id eq appointment.id }) {
            it[clientId] = appointment.clientId
            it[patientId] = appointment.patientId
            it[serviceTypeId] = appointment.serviceTypeId
            it[veterinarianId] = appointment.veterinarianId
            it[date] = kotlinxLocalDate
            it[time] = kotlinxLocalTime
            it[status] = appointment.status
            it[notes] = appointment.notes
            it[updatedAt] = nowLocalDateTime
        }
        
        appointment.copy(updatedAt = now)
    }
    
    override suspend fun delete(id: Int): Boolean = transaction {
        AppointmentsTable.deleteWhere { AppointmentsTable.id eq id } > 0
    }
    
    private fun rowToAppointment(row: ResultRow): Appointment {
        val createdAtLDT = row[AppointmentsTable.createdAt]
        val updatedAtLDT = row[AppointmentsTable.updatedAt]
        val createdAt = createdAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        val updatedAt = updatedAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        
        // Convertir kotlinx.datetime.LocalDate a java.time.LocalDate
        val kotlinxDate = row[AppointmentsTable.date]
        val javaDate = LocalDate.of(
            kotlinxDate.year,
            kotlinxDate.monthNumber,
            kotlinxDate.dayOfMonth
        )
        
        // Convertir kotlinx.datetime.LocalTime a java.time.LocalTime
        val kotlinxTime = row[AppointmentsTable.time]
        val javaTime = LocalTime.of(
            kotlinxTime.hour,
            kotlinxTime.minute,
            kotlinxTime.second
        )
        
        return Appointment(
            id = row[AppointmentsTable.id].value,
            clientId = row[AppointmentsTable.clientId],
            patientId = row[AppointmentsTable.patientId],
            serviceTypeId = row[AppointmentsTable.serviceTypeId],
            veterinarianId = row[AppointmentsTable.veterinarianId],
            date = javaDate,
            time = javaTime,
            status = row[AppointmentsTable.status],
            notes = row[AppointmentsTable.notes],
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun java.time.Instant.toKotlinInstant(): kotlinx.datetime.Instant =
        kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilli())
    
    private fun kotlinx.datetime.Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds())
}

