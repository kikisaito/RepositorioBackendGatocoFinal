package com.example.appointments.domain.ports

import com.example.appointments.domain.models.Appointment

/**
 * Appointment Repository Interface
 * Define las operaciones de acceso a datos para citas
 */
interface AppointmentRepository {
    suspend fun findById(id: Int): Appointment?
    suspend fun findByClientId(clientId: Int): List<Appointment>
    suspend fun findByPatientId(patientId: Int): List<Appointment>
    suspend fun findByVeterinarianId(veterinarianId: Int): List<Appointment>
    suspend fun findAll(): List<Appointment>
    suspend fun save(appointment: Appointment): Appointment
    suspend fun update(appointment: Appointment): Appointment
    suspend fun delete(id: Int): Boolean
}


