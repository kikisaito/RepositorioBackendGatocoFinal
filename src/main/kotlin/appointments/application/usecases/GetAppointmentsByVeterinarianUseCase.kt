package com.example.appointments.application.usecases

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository

/**
 * Use case para obtener todas las citas de un veterinario
 */
class GetAppointmentsByVeterinarianUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(veterinarianId: Int): Result<List<Appointment>> {
        return try {
            val appointments = appointmentRepository.findByVeterinarianId(veterinarianId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

