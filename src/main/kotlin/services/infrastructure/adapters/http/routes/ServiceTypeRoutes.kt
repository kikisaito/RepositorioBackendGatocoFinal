package com.example.services.infrastructure.adapters.http.routes

import com.example.services.infrastructure.adapters.http.controllers.ServiceTypeController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String
)

fun Route.serviceTypeRoutes(controller: ServiceTypeController) {
    route("/api/v1") {
        // Obtener todos los tipos de servicios
        get("/servicios") {
            controller.getAllServiceTypes(call)
        }
    }
}


