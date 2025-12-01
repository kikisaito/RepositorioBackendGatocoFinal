package com.example.appointments.application.usecases

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository
import java.time.LocalDate
import java.time.LocalTime

/**
 * Use case para crear una nueva cita
 */
class CreateAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        clientId: Int,
        patientId: Int,
        serviceTypeId: Int,
        veterinarianId: Int,
        date: LocalDate,
        time: LocalTime,
        notes: String? = null
    ): Result<Appointment> {
        return try {
            val appointment = Appointment(
                id = 0, // Se asignará al guardar
                clientId = clientId,
                patientId = patientId,
                serviceTypeId = serviceTypeId,
                veterinarianId = veterinarianId,
                date = date,
                time = time,
                status = "pendiente",
                notes = notes
            )
            
            val savedAppointment = appointmentRepository.save(appointment)
            Result.success(savedAppointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}












