package com.example.appointments.domain.models

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Appointment Domain Model
 * Representa una cita en el sistema
 */
data class Appointment(
    val id: Int,
    val clientId: Int,
    val patientId: Int,
    val serviceTypeId: Int,
    val veterinarianId: Int,
    val date: LocalDate,
    val time: LocalTime,
    val status: String = "pendiente", // pendiente, confirmada, completada, cancelada
    val notes: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)












