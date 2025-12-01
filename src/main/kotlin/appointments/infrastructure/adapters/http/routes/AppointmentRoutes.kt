package com.example.appointments.infrastructure.adapters.http.routes

import com.example.appointments.infrastructure.adapters.http.controllers.AppointmentController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String
)

fun Route.appointmentRoutes(controller: AppointmentController) {
    route("/api/v1") {
        // Obtener todas las citas de un cliente o veterinario
        get("/citas") {
            val clienteId = call.request.queryParameters["clienteId"]?.toIntOrNull()
            val duenoId = call.request.queryParameters["duenoId"]?.toIntOrNull()
            val veterinarioId = call.request.queryParameters["veterinarioId"]?.toIntOrNull()
            
            // Si hay veterinarioId, obtener citas del veterinario
            if (veterinarioId != null) {
                controller.getAppointmentsByVeterinarianId(call, veterinarioId)
                return@get
            }
            
            // El frontend puede enviar clienteId (userId) o duenoId (userId)
            val userId = duenoId ?: clienteId
            
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "El parámetro 'clienteId', 'duenoId' o 'veterinarioId' es requerido")
                )
                return@get
            }
            
            // Intentar obtener citas por userId (puede ser cliente o veterinario)
            controller.getAppointmentsByUserId(call, userId)
        }
        
        // Crear una nueva cita
        post("/citas") {
            controller.createAppointment(call)
        }
        
        // Obtener una cita por ID
        get("/citas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la cita es requerido")
                    )
                    return@get
                }
            // TODO: Implementar getAppointmentById
            call.respond(
                HttpStatusCode.NotImplemented,
                ErrorResponse(message = "Endpoint no implementado aún")
            )
        }
        
        // Actualizar el estado de una cita
        put("/citas/{id}/estado") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la cita es requerido")
                    )
                    return@put
                }
            
            val newStatus = call.request.queryParameters["estado"]
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El parámetro 'estado' es requerido")
                    )
                    return@put
                }
            
            controller.updateAppointmentStatus(call, id, newStatus)
        }
        
        // Actualizar una cita completa
        put("/citas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la cita es requerido")
                    )
                    return@put
                }
            
            controller.updateAppointment(call, id)
        }
    }
}

