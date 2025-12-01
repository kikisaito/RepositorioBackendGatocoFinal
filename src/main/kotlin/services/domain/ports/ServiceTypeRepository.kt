package com.example.services.domain.ports

import com.example.services.domain.models.ServiceType

/**
 * ServiceType Repository Interface
 * Define las operaciones de acceso a datos para tipos de servicios
 */
interface ServiceTypeRepository {
    suspend fun findAll(): List<ServiceType>
    suspend fun findById(id: Int): ServiceType?
}


