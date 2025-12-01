package com.example.appointments.application.usecases

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository

/**
 * Use case para actualizar el estado de una cita
 */
class UpdateAppointmentStatusUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: Int,
        newStatus: String
    ): Result<Appointment> {
        return try {
            // Validar que la cita exista
            val existingAppointment = appointmentRepository.findById(appointmentId)
                ?: return Result.failure(Exception("La cita no existe"))
            
            // Validar que el estado sea válido
            val validStatuses = listOf("pendiente", "cancelada", "completada")
            if (!validStatuses.contains(newStatus.lowercase())) {
                return Result.failure(Exception("Estado inválido. Los estados válidos son: pendiente, cancelada, completada"))
            }
            
            // Actualizar el estado de la cita
            val updatedAppointment = existingAppointment.copy(status = newStatus.lowercase())
            val savedAppointment = appointmentRepository.update(updatedAppointment)
            
            Result.success(savedAppointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

