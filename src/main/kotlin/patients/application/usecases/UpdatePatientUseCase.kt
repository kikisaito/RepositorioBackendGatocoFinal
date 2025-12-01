package com.example.patients.application.usecases

import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.models.Patient
import com.example.patients.domain.ports.PatientRepository
import java.time.LocalDate

/**
 * UpdatePatientUseCase
 * Caso de uso para actualizar un paciente (mascota) existente
 */
class UpdatePatientUseCase(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        patientId: Int,
        name: String,
        species: String,
        breed: String? = null,
        birthDate: LocalDate? = null,
        gender: String? = null,
        weight: Double? = null,
        photoUrl: String? = null
    ): Result<Patient> {
        return try {
            // Validaciones básicas
            if (name.isBlank()) {
                return Result.failure(PatientException("El nombre de la mascota no puede estar vacío"))
            }
            
            if (species.isBlank()) {
                return Result.failure(PatientException("La especie de la mascota no puede estar vacía"))
            }
            
            // Buscar el paciente existente
            val existingPatient = patientRepository.findById(patientId)
                ?: return Result.failure(PatientException("La mascota no existe"))
            
            // Actualizar paciente
            val updatedPatient = existingPatient.copy(
                name = name.trim(),
                species = species.trim(),
                breed = breed?.trim(),
                birthDate = birthDate,
                gender = gender?.trim(),
                weight = weight,
                photoUrl = photoUrl ?: existingPatient.photoUrl
            )
            
            val savedPatient = patientRepository.update(updatedPatient)
            
            Result.success(savedPatient)
        } catch (e: PatientException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PatientException("Error al actualizar la mascota: ${e.message}"))
        }
    }
}

