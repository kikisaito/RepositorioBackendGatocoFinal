package com.example.patients.application.usecases

import com.example.appointments.domain.ports.AppointmentRepository
import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.ports.PatientRepository

/**
 * DeletePatientUseCase
 * Caso de uso para eliminar un paciente (mascota)
 */
class DeletePatientUseCase(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(patientId: Int): Result<Boolean> {
        return try {
            // Verificar que la mascota existe
            val existingPatient = patientRepository.findById(patientId)
                ?: return Result.failure(PatientException("La mascota no existe"))
            
            // Verificar si hay citas asociadas a esta mascota
            val appointments = appointmentRepository.findByPatientId(patientId)
            if (appointments.isNotEmpty()) {
                return Result.failure(
                    PatientException(
                        "No se puede eliminar la mascota porque tiene ${appointments.size} cita(s) asociada(s). " +
                        "Por favor, elimine o cancele las citas primero."
                    )
                )
            }
            
            // Eliminar mascota
            val deleted = patientRepository.delete(patientId)
            
            if (deleted) {
                Result.success(true)
            } else {
                Result.failure(PatientException("No se pudo eliminar la mascota"))
            }
        } catch (e: PatientException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PatientException("Error al eliminar la mascota: ${e.message}"))
        }
    }
}

