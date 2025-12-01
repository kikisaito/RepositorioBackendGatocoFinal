package com.example.services.infrastructure.adapters.http.controllers

import com.example.services.application.usecases.GetAllServiceTypesUseCase
import com.example.services.infrastructure.adapters.http.responses.ServiceTypeResponse
import com.example.auth.infrastructure.adapters.http.controllers.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond

class ServiceTypeController(
    private val getAllServiceTypesUseCase: GetAllServiceTypesUseCase
) {
    
    suspend fun getAllServiceTypes(call: ApplicationCall) {
        try {
            val result = getAllServiceTypesUseCase.invoke()
            
            result.fold(
                onSuccess = { serviceTypes ->
                    val serviceTypesResponse = serviceTypes.map { serviceTypeToResponse(it) }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = serviceTypesResponse,
                            message = "Servicios obtenidos exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<List<ServiceTypeResponse>>(
                            success = false,
                            message = error.message ?: "Error al obtener los servicios"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getAllServiceTypes: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<ServiceTypeResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    private fun serviceTypeToResponse(serviceType: com.example.services.domain.models.ServiceType): ServiceTypeResponse {
        return ServiceTypeResponse(
            id = serviceType.id,
            name = serviceType.name
        )
    }
}


