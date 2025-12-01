package com.example.patients.application.usecases

import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.models.Patient
import com.example.patients.domain.ports.PatientRepository

/**
 * GetPatientByIdUseCase
 * Caso de uso para obtener un paciente por su ID
 */
class GetPatientByIdUseCase(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(patientId: Int): Result<Patient> {
        return try {
            val patient = patientRepository.findById(patientId)
                ?: return Result.failure(PatientException("La mascota no existe"))
            
            Result.success(patient)
        } catch (e: PatientException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PatientException("Error al obtener la mascota: ${e.message}"))
        }
    }
}

