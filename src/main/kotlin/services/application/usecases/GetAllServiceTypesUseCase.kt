package com.example.services.application.usecases

import com.example.services.domain.ports.ServiceTypeRepository

/**
 * Use case para obtener todos los tipos de servicios
 */
class GetAllServiceTypesUseCase(
    private val serviceTypeRepository: ServiceTypeRepository
) {
    suspend operator fun invoke(): Result<List<com.example.services.domain.models.ServiceType>> {
        return try {
            val serviceTypes = serviceTypeRepository.findAll()
            Result.success(serviceTypes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


