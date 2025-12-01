package com.example.appointments.application.usecases

import com.example.appointments.domain.models.Appointment
import com.example.appointments.domain.ports.AppointmentRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Use case para actualizar una cita completa
 */
class UpdateAppointmentUseCase(
    private val appointmentRepository: AppointmentRepository
) {
    suspend operator fun invoke(
        appointmentId: Int,
        diagnostico: String? = null,
        tratamiento: String? = null,
        estado: String? = null,
        informacionMascota: Map<String, Any>? = null
    ): Result<Appointment> {
        return try {
            // Validar que la cita exista
            val existingAppointment = appointmentRepository.findById(appointmentId)
                ?: return Result.failure(Exception("La cita no existe"))
            
            // Construir el objeto JSON para notes
            val notesJson = buildJsonObject {
                // Si hay notas existentes, intentar parsearlas y preservar campos que no se están actualizando
                if (existingAppointment.notes != null) {
                    try {
                        val existingNotes = Json.parseToJsonElement(existingAppointment.notes) as? JsonObject
                        existingNotes?.forEach { (key, value) ->
                            // Solo preservar campos que no sean diagnóstico o tratamiento
                            if (key != "diagnostico" && key != "tratamiento") {
                                put(key, value)
                            }
                        }
                    } catch (e: Exception) {
                        // Si las notas existentes no son JSON válido, preservarlas en un campo especial
                        put("notas_anteriores", existingAppointment.notes)
                    }
                }
                
                // Agregar diagnóstico y tratamiento si se proporcionan
                if (diagnostico != null) {
                    put("diagnostico", diagnostico)
                }
                if (tratamiento != null) {
                    put("tratamiento", tratamiento)
                }
                
                // Agregar información de la mascota en el momento de la consulta
                if (informacionMascota != null) {
                    val mascotaJson = buildJsonObject {
                        informacionMascota.forEach { (key, value) ->
                            when (value) {
                                is String -> put(key, value)
                                is Number -> put(key, value)
                                else -> put(key, value.toString())
                            }
                        }
                    }
                    put("informacionMascota", mascotaJson)
                }
            }
            
            val notesString = if (notesJson.isEmpty()) null else notesJson.toString()
            
            // Validar que el estado sea válido si se proporciona
            val newStatus = if (estado != null) {
                val validStatuses = listOf("pendiente", "cancelada", "completada")
                if (!validStatuses.contains(estado.lowercase())) {
                    return Result.failure(Exception("Estado inválido. Los estados válidos son: pendiente, cancelada, completada"))
                }
                estado.lowercase()
            } else {
                existingAppointment.status
            }
            
            // Actualizar la cita
            val updatedAppointment = existingAppointment.copy(
                status = newStatus,
                notes = notesString
            )
            val savedAppointment = appointmentRepository.update(updatedAppointment)
            
            Result.success(savedAppointment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

