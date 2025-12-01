package com.example.appointments.infrastructure.adapters.http.controllers

import com.example.appointments.application.usecases.CreateAppointmentUseCase
import com.example.appointments.application.usecases.GetAppointmentsByClientUseCase
import com.example.appointments.application.usecases.GetAppointmentsByVeterinarianUseCase
import com.example.appointments.application.usecases.UpdateAppointmentStatusUseCase
import com.example.appointments.application.usecases.UpdateAppointmentUseCase
import com.example.appointments.domain.ports.AppointmentRepository
import com.example.appointments.infrastructure.adapters.http.requests.CreateAppointmentRequest
import com.example.appointments.infrastructure.adapters.http.requests.UpdateAppointmentRequest
import com.example.appointments.infrastructure.adapters.http.responses.AppointmentResponse
import com.example.auth.infrastructure.adapters.http.controllers.ApiResponse
import com.example.auth.domain.ports.ClientRepository
import com.example.patients.domain.ports.PatientRepository
import com.example.services.domain.ports.ServiceTypeRepository
import com.example.auth.domain.ports.VeterinarianRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import java.time.LocalDate
import java.time.LocalTime

class AppointmentController(
    private val createAppointmentUseCase: CreateAppointmentUseCase,
    private val getAppointmentsByClientUseCase: GetAppointmentsByClientUseCase,
    private val getAppointmentsByVeterinarianUseCase: GetAppointmentsByVeterinarianUseCase,
    private val updateAppointmentStatusUseCase: UpdateAppointmentStatusUseCase,
    private val updateAppointmentUseCase: UpdateAppointmentUseCase,
    private val appointmentRepository: AppointmentRepository,
    private val clientRepository: ClientRepository,
    private val patientRepository: PatientRepository,
    private val serviceTypeRepository: ServiceTypeRepository,
    private val veterinarianRepository: VeterinarianRepository
) {
    
    suspend fun createAppointment(call: ApplicationCall) {
        try {
            val request = call.receive<CreateAppointmentRequest>()
            
            // Obtener userId o clientId del request
            // El frontend puede enviar userId (duenoId/clienteId) o clientId directamente
            val userId = request.duenoId ?: request.clienteId
            
            var client: com.example.auth.domain.models.Client? = null
            var clientId: Int
            
            if (userId != null) {
                // Intentar buscar el cliente por userId primero
                client = clientRepository.findByUserId(userId)
                
                if (client == null) {
                    // Si no se encuentra por userId, intentar buscar por clientId
                    // (en caso de que el frontend envíe clientId como userId)
                    client = clientRepository.findById(userId)
                }
            }
            
            if (client == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "No se encontró información del cliente para este usuario")
                )
                return
            }
            
            clientId = client.id
            
            // Validar que existan las entidades relacionadas
            val patient = patientRepository.findById(request.mascotaId)
            if (patient == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "La mascota no existe")
                )
                return
            }
            
            val serviceType = serviceTypeRepository.findById(request.servicioId)
            if (serviceType == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "El servicio no existe")
                )
                return
            }
            
            val veterinarian = veterinarianRepository.findById(request.veterinarioId)
            if (veterinarian == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "El veterinario no existe")
                )
                return
            }
            
            // Parsear fecha y hora
            val date = try {
                LocalDate.parse(request.fecha)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "Formato de fecha inválido. Use el formato YYYY-MM-DD")
                )
                return
            }
            
            val time = try {
                LocalTime.parse(request.hora)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<AppointmentResponse>(success = false, message = "Formato de hora inválido. Use el formato HH:mm")
                )
                return
            }
            
            val result = createAppointmentUseCase.invoke(
                clientId = clientId,
                patientId = request.mascotaId,
                serviceTypeId = request.servicioId,
                veterinarianId = request.veterinarioId,
                date = date,
                time = time,
                notes = request.notas
            )
            
            result.fold(
                onSuccess = { appointment ->
                    val appointmentResponse = appointmentToResponse(
                        appointment,
                        patient.name,
                        serviceType.name,
                        veterinarian.fullName,
                        client.fullName,
                        patient.photoUrl
                    )
                    
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            data = appointmentResponse,
                            message = "Cita creada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<AppointmentResponse>(
                            success = false,
                            message = error.message ?: "Error al crear la cita"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en createAppointment: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<AppointmentResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getAppointmentsByUserId(call: ApplicationCall, userId: Int) {
        try {
            // Primero intentar buscar si es un veterinario
            val veterinarian = veterinarianRepository.findByUserId(userId)
            
            if (veterinarian != null) {
                // Es un veterinario, obtener sus citas
                val veterinarianId = veterinarian.id
                getAppointmentsByVeterinarianId(call, veterinarianId)
                return
            }
            
            // Si no es veterinario, buscar si es un cliente
            val client = clientRepository.findByUserId(userId)
            
            if (client == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<List<AppointmentResponse>>(
                        success = false,
                        message = "No se encontró información del cliente o veterinario para este usuario"
                    )
                )
                return
            }
            
            val clientId = client.id
            getAppointmentsByClient(call, clientId)
        } catch (e: Exception) {
            println("Error en getAppointmentsByUserId: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<AppointmentResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getAppointmentsByClient(call: ApplicationCall, clientId: Int) {
        try {
            val result = getAppointmentsByClientUseCase.invoke(clientId)
            
            result.fold(
                onSuccess = { appointments ->
                    val appointmentsResponse = appointments.map { appointment ->
                        // Obtener datos relacionados
                        val patient = patientRepository.findById(appointment.patientId)
                        val serviceType = serviceTypeRepository.findById(appointment.serviceTypeId)
                        val veterinarian = veterinarianRepository.findById(appointment.veterinarianId)
                        val client = clientRepository.findById(appointment.clientId)
                        
                        appointmentToResponse(
                            appointment,
                            patient?.name ?: "Desconocido",
                            serviceType?.name ?: "Desconocido",
                            veterinarian?.fullName ?: "Desconocido",
                            client?.fullName ?: "Desconocido",
                            patient?.photoUrl
                        )
                    }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = appointmentsResponse,
                            message = "Citas obtenidas exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<List<AppointmentResponse>>(
                            success = false,
                            message = error.message ?: "Error al obtener las citas"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getAppointmentsByClient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<AppointmentResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getAppointmentsByVeterinarianId(call: ApplicationCall, veterinarianId: Int) {
        try {
            // El veterinarioId puede ser el ID de la tabla veterinarios o el userId
            // Intentar buscar primero por ID de veterinario
            var veterinarian = veterinarianRepository.findById(veterinarianId)
            
            // Si no se encuentra por ID, intentar buscar por userId
            if (veterinarian == null) {
                veterinarian = veterinarianRepository.findByUserId(veterinarianId)
            }
            
            if (veterinarian == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ApiResponse<List<AppointmentResponse>>(
                        success = false,
                        message = "El veterinario no existe"
                    )
                )
                return
            }
            
            // Usar el ID real del veterinario (de la tabla veterinarios)
            val realVeterinarianId = veterinarian.id
            val result = getAppointmentsByVeterinarianUseCase.invoke(realVeterinarianId)
            
            result.fold(
                onSuccess = { appointments ->
                    val appointmentsResponse = appointments.map { appointment ->
                        // Obtener datos relacionados
                        val patient = patientRepository.findById(appointment.patientId)
                        val serviceType = serviceTypeRepository.findById(appointment.serviceTypeId)
                        val veterinarian = veterinarianRepository.findById(appointment.veterinarianId)
                        val client = clientRepository.findById(appointment.clientId)
                        
                        appointmentToResponse(
                            appointment,
                            patient?.name ?: "Desconocido",
                            serviceType?.name ?: "Desconocido",
                            veterinarian?.fullName ?: "Desconocido",
                            client?.fullName ?: "Desconocido",
                            patient?.photoUrl
                        )
                    }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = appointmentsResponse,
                            message = "Citas obtenidas exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<List<AppointmentResponse>>(
                            success = false,
                            message = error.message ?: "Error al obtener las citas"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getAppointmentsByVeterinarianId: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<AppointmentResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun updateAppointmentStatus(call: ApplicationCall, appointmentId: Int, newStatus: String) {
        try {
            // Validar que la cita exista
            val existingAppointment = appointmentRepository.findById(appointmentId)
                ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<AppointmentResponse>(
                            success = false,
                            message = "La cita no existe"
                        )
                    )
                    return
                }
            
            // Validar que el usuario tenga permiso para actualizar esta cita
            // (el cliente debe ser el dueño de la cita)
            // Esto se puede validar con el token JWT en el futuro
            
            val result = updateAppointmentStatusUseCase.invoke(appointmentId, newStatus)
            
            result.fold(
                onSuccess = { updatedAppointment ->
                    // Obtener datos relacionados para la respuesta
                    val patient = patientRepository.findById(updatedAppointment.patientId)
                    val serviceType = serviceTypeRepository.findById(updatedAppointment.serviceTypeId)
                    val veterinarian = veterinarianRepository.findById(updatedAppointment.veterinarianId)
                    val client = clientRepository.findById(updatedAppointment.clientId)
                    
                    val appointmentResponse = appointmentToResponse(
                        updatedAppointment,
                        patient?.name ?: "Desconocido",
                        serviceType?.name ?: "Desconocido",
                        veterinarian?.fullName ?: "Desconocido",
                        client?.fullName ?: "Desconocido",
                        patient?.photoUrl
                    )
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = appointmentResponse,
                            message = "Estado de la cita actualizado exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<AppointmentResponse>(
                            success = false,
                            message = error.message ?: "Error al actualizar el estado de la cita"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en updateAppointmentStatus: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<AppointmentResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun updateAppointment(call: ApplicationCall, appointmentId: Int) {
        try {
            val request = call.receive<UpdateAppointmentRequest>()
            
            // Validar que la cita exista
            val existingAppointment = appointmentRepository.findById(appointmentId)
                ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<AppointmentResponse>(
                            success = false,
                            message = "La cita no existe"
                        )
                    )
                    return
                }
            
            // Obtener información de la mascota en el momento de la consulta
            val informacionMascota = if (request.informacionMascota != null) {
                val edad = request.informacionMascota.edad
                val fechaNacimiento = request.informacionMascota.fechaNacimiento
                // Solo incluir edad si es mayor que 0, de lo contrario se calculará desde fechaNacimiento
                val map = mutableMapOf<String, Any>(
                    "nombre" to request.informacionMascota.nombre,
                    "especie" to request.informacionMascota.especie
                )
                // Solo agregar campos si tienen valor
                if (request.informacionMascota.raza != null && request.informacionMascota.raza.isNotBlank()) {
                    map["raza"] = request.informacionMascota.raza
                }
                if (fechaNacimiento != null && fechaNacimiento.isNotBlank()) {
                    map["fechaNacimiento"] = fechaNacimiento
                }
                if (request.informacionMascota.sexo != null && request.informacionMascota.sexo.isNotBlank()) {
                    map["sexo"] = request.informacionMascota.sexo
                }
                // Solo agregar edad si es mayor que 0
                if (edad != null && edad > 0) {
                    map["edad"] = edad
                }
                map
            } else {
                null
            }
            
            // Actualizar la cita usando el use case
            val result = updateAppointmentUseCase.invoke(
                appointmentId = appointmentId,
                diagnostico = request.diagnostico,
                tratamiento = request.tratamiento,
                estado = request.estado,
                informacionMascota = informacionMascota
            )
            
            result.fold(
                onSuccess = { updatedAppointment ->
                    // Obtener datos relacionados para la respuesta
                    val patient = patientRepository.findById(updatedAppointment.patientId)
                    val serviceType = serviceTypeRepository.findById(updatedAppointment.serviceTypeId)
                    val veterinarian = veterinarianRepository.findById(updatedAppointment.veterinarianId)
                    val client = clientRepository.findById(updatedAppointment.clientId)
                    
                    val appointmentResponse = appointmentToResponse(
                        updatedAppointment,
                        patient?.name ?: "Desconocido",
                        serviceType?.name ?: "Desconocido",
                        veterinarian?.fullName ?: "Desconocido",
                        client?.fullName ?: "Desconocido",
                        patient?.photoUrl
                    )
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = appointmentResponse,
                            message = "Cita actualizada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<AppointmentResponse>(
                            success = false,
                            message = error.message ?: "Error al actualizar la cita"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en updateAppointment: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<AppointmentResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    private fun appointmentToResponse(
        appointment: com.example.appointments.domain.models.Appointment,
        patientName: String,
        serviceName: String,
        veterinarianName: String,
        clientName: String,
        patientPhotoUrl: String? = null
    ): AppointmentResponse {
        // Formatear la hora en formato HH:mm
        val horaFormateada = String.format("%02d:%02d", appointment.time.hour, appointment.time.minute)
        
        // Log para debug
        if (patientPhotoUrl != null) {
            println("AppointmentController - Cita ${appointment.id} con foto: $patientPhotoUrl para mascota: $patientName")
        }
        
        return AppointmentResponse(
            id = appointment.id,
            mascotaId = appointment.patientId,
            mascota = patientName,
            mascotaFoto = patientPhotoUrl,
            servicioId = appointment.serviceTypeId,
            servicio = serviceName,
            veterinarioId = appointment.veterinarianId,
            veterinario = veterinarianName,
            clienteId = appointment.clientId,
            cliente = clientName,
            fecha = appointment.date.toString(),
            hora = horaFormateada,
            estado = appointment.status,
            notas = appointment.notes,
            createdAt = appointment.createdAt.epochSecond * 1000
        )
    }
}

