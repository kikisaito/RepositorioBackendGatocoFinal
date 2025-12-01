package com.example.patients.infrastructure.adapters.http.routes

import com.example.patients.infrastructure.adapters.http.controllers.PatientController
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String
)

fun Route.patientRoutes(controller: PatientController) {
    route("/api/v1") {
        // Obtener todas las mascotas de un cliente
        // Frontend envía ?duenoId= que es el userId, pero backend necesita clientId
        get("/mascotas") {
            val duenoId = call.request.queryParameters["duenoId"]?.toIntOrNull()
            val clientId = call.request.queryParameters["clienteId"]?.toIntOrNull()
            
            if (duenoId != null) {
                // Si es duenoId (userId), buscar el client asociado
                controller.getPatientsByUserId(call, duenoId)
            } else if (clientId != null) {
                // Si es clientId directo, usar directamente
                controller.getPatientsByClient(call, clientId)
            } else {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(message = "El parámetro 'duenoId' o 'clienteId' es requerido")
                )
                return@get
            }
        }
        
        // Registrar una nueva mascota
        // Frontend envía POST /api/v1/mascotas con duenoId o clientId en el body
        post("/mascotas") {
            controller.createPatient(call)
        }
        
        // Obtener una mascota por ID
        get("/mascotas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la mascota es requerido")
                    )
                    return@get
                }
            controller.getPatientById(call, id)
        }
        
        // Actualizar una mascota
        put("/mascotas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la mascota es requerido")
                    )
                    return@put
                }
            controller.updatePatient(call, id)
        }
        
        // Eliminar una mascota
        delete("/mascotas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la mascota es requerido")
                    )
                    return@delete
                }
            controller.deletePatient(call, id)
        }
        
        // Subir/Actualizar foto de una mascota
        post("/mascotas/{id}/foto") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la mascota es requerido")
                    )
                    return@post
                }
            controller.uploadPhoto(call, id)
        }
        
        // Eliminar foto de una mascota
        delete("/mascotas/{id}/foto") {
            val id = call.parameters["id"]?.toIntOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(message = "El ID de la mascota es requerido")
                    )
                    return@delete
                }
            controller.deletePhoto(call, id)
        }
    }
}

