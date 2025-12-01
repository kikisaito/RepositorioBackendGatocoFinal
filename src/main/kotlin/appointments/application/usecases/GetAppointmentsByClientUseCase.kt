package com.example.appointments.application.usecases

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository

/**
 * Use case para obtener todas las citas de un cliente
 */
class GetAppointmentsByClientUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(clientId: Int): Result<List<Appointment>> {
        return try {
            val appointments = appointmentRepository.findByClientId(clientId)
            Result.success(appointments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}












