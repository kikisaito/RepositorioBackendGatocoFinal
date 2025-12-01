package com.example.patients.application.usecases

import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.models.Patient
import com.example.patients.domain.ports.PatientRepository

/**
 * GetPatientsByClientUseCase
 * Caso de uso para obtener todas las mascotas de un cliente
 */
class GetPatientsByClientUseCase(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(clientId: Int): Result<List<Patient>> {
        return try {
            // Validar que clientId sea válido
            if (clientId <= 0) {
                return Result.failure(PatientException("El ID del cliente no es válido"))
            }
            
            val patients = patientRepository.findByClientId(clientId)
            
            Result.success(patients)
        } catch (e: PatientException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PatientException("Error al obtener las mascotas: ${e.message}"))
        }
    }
}


