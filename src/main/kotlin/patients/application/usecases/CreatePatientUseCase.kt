package com.example.patients.application.usecases

import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.models.Patient
import com.example.patients.domain.ports.PatientRepository
import java.time.LocalDate

/**
 * CreatePatientUseCase
 * Caso de uso para crear un nuevo paciente (mascota)
 */
class CreatePatientUseCase(
    private val patientRepository: PatientRepository
) {
    suspend operator fun invoke(
        clientId: Int,
        name: String,
        species: String,
        breed: String? = null,
        birthDate: LocalDate? = null,
        gender: String? = null,
        weight: Double? = null
    ): Result<Patient> {
        return try {
            // Validaciones básicas
            if (name.isBlank()) {
                return Result.failure(PatientException("El nombre de la mascota no puede estar vacío"))
            }
            
            if (species.isBlank()) {
                return Result.failure(PatientException("La especie de la mascota no puede estar vacía"))
            }
            
            // Crear paciente
            val patient = Patient(
                id = 0,
                clientId = clientId,
                name = name.trim(),
                species = species.trim(),
                breed = breed?.trim(),
                birthDate = birthDate,
                gender = gender?.trim(),
                weight = weight
            )
            
            val savedPatient = patientRepository.save(patient)
            
            Result.success(savedPatient)
        } catch (e: PatientException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(PatientException("Error al registrar la mascota: ${e.message}"))
        }
    }
}


