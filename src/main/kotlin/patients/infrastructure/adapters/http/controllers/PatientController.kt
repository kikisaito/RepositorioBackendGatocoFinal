package com.example.patients.infrastructure.adapters.http.controllers

import com.example.patients.application.usecases.CreatePatientUseCase
import com.example.patients.application.usecases.GetPatientsByClientUseCase
import com.example.patients.application.usecases.UpdatePatientUseCase
import com.example.patients.application.usecases.DeletePatientUseCase
import com.example.patients.application.usecases.GetPatientByIdUseCase
import com.example.patients.domain.exceptions.PatientException
import com.example.patients.domain.models.Patient
import com.example.patients.infrastructure.adapters.http.requests.CreatePatientRequest
import com.example.patients.infrastructure.adapters.http.requests.UpdatePatientRequest
import com.example.patients.infrastructure.adapters.http.responses.PatientResponse
import com.example.patients.infrastructure.services.CloudinaryService
import com.example.auth.infrastructure.adapters.http.controllers.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.core.readBytes
import java.time.LocalDate

class PatientController(
    private val createPatientUseCase: CreatePatientUseCase,
    private val getPatientsByClientUseCase: GetPatientsByClientUseCase,
    private val clientRepository: com.example.auth.domain.ports.ClientRepository,
    private val updatePatientUseCase: UpdatePatientUseCase,
    private val deletePatientUseCase: DeletePatientUseCase,
    private val getPatientByIdUseCase: GetPatientByIdUseCase,
    private val cloudinaryService: CloudinaryService
) {
    
    suspend fun createPatient(call: ApplicationCall) {
        try {
            val request = call.receive<CreatePatientRequest>()
            
            // Obtener userId del request (frontend envía duenoId que es el user.id)
            val userId = request.duenoId ?: request.clientId
            
            if (userId == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "El campo 'duenoId' es requerido")
                )
                return
            }
            
            // Buscar el client asociado al userId
            val client = clientRepository.findByUserId(userId)
            
            if (client == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "No se encontró información del cliente para este usuario")
                )
                return
            }
            
            val clientId = client.id
            
            // Validar campos requeridos
            if (request.nombre.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "El nombre es requerido")
                )
                return
            }
            
            if (request.especie.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "La especie es requerida")
                )
                return
            }
            
            // Parse birthDate if provided
            val birthDate = request.fechaNacimiento?.let { 
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = "Formato de fecha inválido. Use el formato YYYY-MM-DD"
                        )
                    )
                    return
                }
            }
            
            val result = createPatientUseCase.invoke(
                clientId = clientId,
                name = request.nombre,
                species = request.especie,
                breed = request.raza,
                birthDate = birthDate,
                gender = request.sexo,
                weight = request.peso
            )
            
            result.fold(
                onSuccess = { patient ->
                    val patientResponse = patientToResponse(patient)
                    
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            data = patientResponse,
                            message = "Mascota registrada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en createPatient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<PatientResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getPatientsByUserId(call: ApplicationCall, userId: Int) {
        try {
            // Buscar el client asociado al userId
            val client = clientRepository.findByUserId(userId)
            
            if (client == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<List<PatientResponse>>(
                        success = false,
                        message = "No se encontró información del cliente para este usuario"
                    )
                )
                return
            }
            
            val result = getPatientsByClientUseCase.invoke(client.id)
            
            result.fold(
                onSuccess = { patients ->
                    val patientsResponse = patients.map { patientToResponse(it) }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientsResponse,
                            message = "Mascotas obtenidas exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<List<PatientResponse>>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getPatientsByUserId: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<PatientResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getPatientsByClient(call: ApplicationCall, clientId: Int) {
        try {
            val result = getPatientsByClientUseCase.invoke(clientId)
            
            result.fold(
                onSuccess = { patients ->
                    val patientsResponse = patients.map { patientToResponse(it) }
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientsResponse,
                            message = "Mascotas obtenidas exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<List<PatientResponse>>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getPatientsByClient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<PatientResponse>>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun updatePatient(call: ApplicationCall, patientId: Int) {
        try {
            val request = call.receive<UpdatePatientRequest>()
            
            // Validar que al menos un campo esté presente
            if (request.nombre.isNullOrBlank() && request.especie.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "Al menos el nombre o la especie son requeridos")
                )
                return
            }
            
            // Obtener el paciente actual
            val existingPatient = getPatientByIdUseCase.invoke(patientId).getOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<PatientResponse>(success = false, message = "La mascota no existe")
                    )
                    return
                }
            
            // Parsear fecha si se proporciona
            val birthDate = request.fechaNacimiento?.let { 
                try {
                    LocalDate.parse(it)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = "Formato de fecha inválido. Use el formato YYYY-MM-DD"
                        )
                    )
                    return
                }
            } ?: existingPatient.birthDate
            
            val result = updatePatientUseCase.invoke(
                patientId = patientId,
                name = request.nombre ?: existingPatient.name,
                species = request.especie ?: existingPatient.species,
                breed = request.raza ?: existingPatient.breed,
                birthDate = birthDate,
                gender = request.sexo ?: existingPatient.gender,
                weight = request.peso ?: existingPatient.weight,
                photoUrl = existingPatient.photoUrl // Mantener la foto existente si no se actualiza
            )
            
            result.fold(
                onSuccess = { patient ->
                    val patientResponse = patientToResponse(patient)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientResponse,
                            message = "Mascota actualizada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en updatePatient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<PatientResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun deletePatient(call: ApplicationCall, patientId: Int) {
        try {
            val result = deletePatientUseCase.invoke(patientId)
            
            result.fold(
                onSuccess = { deleted ->
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse<String>(
                            success = true,
                            message = "Mascota eliminada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<String>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en deletePatient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<String>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun getPatientById(call: ApplicationCall, patientId: Int) {
        try {
            val result = getPatientByIdUseCase.invoke(patientId)
            
            result.fold(
                onSuccess = { patient ->
                    val patientResponse = patientToResponse(patient)
                    
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientResponse,
                            message = "Mascota obtenida exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is PatientException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en getPatientById: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<PatientResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun uploadPhoto(call: ApplicationCall, patientId: Int) {
        try {
            // Verificar que el paciente existe
            val existingPatient = getPatientByIdUseCase.invoke(patientId).getOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<PatientResponse>(success = false, message = "La mascota no existe")
                    )
                    return
                }
            
            // Recibir el multipart
            val multipart = call.receiveMultipart()
            var photoPart: PartData.FileItem? = null
            var bytes: ByteArray? = null
            
            // Buscar la parte del archivo y leer los bytes dentro del forEachPart
            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.name == "photo" || part.name == "file") {
                            // Validar tipo de archivo
                            val contentType = part.contentType?.toString() ?: ""
                            if (!contentType.startsWith("image/")) {
                                part.dispose()
                                return@forEachPart
                            }
                            
                            // Leer los bytes del archivo ANTES de asignar photoPart
                            // En Ktor 3.x, necesitamos leer el canal dentro del forEachPart
                            val channel = part.provider()
                            bytes = channel.readRemaining().readBytes()
                            photoPart = part
                            
                            println("DEBUG: Bytes leídos: ${bytes?.size ?: 0} bytes, contentType: $contentType")
                        } else {
                            part.dispose()
                        }
                    }
                    else -> part.dispose()
                }
            }
            
            if (photoPart == null || bytes == null || bytes.isEmpty()) {
                photoPart?.dispose()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "No se encontró el archivo de imagen o el archivo está vacío")
                )
                return
            }
            
            // Si ya existe una foto, eliminarla de Cloudinary
            existingPatient.photoUrl?.let { oldPhotoUrl ->
                cloudinaryService.deleteImage(oldPhotoUrl)
            }
            
            // Subir nueva imagen a Cloudinary
            val publicId = "pet_${patientId}_${System.currentTimeMillis()}"
            val photoUrl = cloudinaryService.uploadImage(
                bytes,
                publicId
            )
            
            photoPart!!.dispose()
            
            // Actualizar el paciente con la nueva URL
            val result = updatePatientUseCase.invoke(
                patientId = patientId,
                name = existingPatient.name,
                species = existingPatient.species,
                breed = existingPatient.breed,
                birthDate = existingPatient.birthDate,
                gender = existingPatient.gender,
                weight = existingPatient.weight,
                photoUrl = photoUrl
            )
            
            result.fold(
                onSuccess = { patient ->
                    val patientResponse = patientToResponse(patient)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientResponse,
                            message = "Foto actualizada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = error.message ?: "Error al actualizar la foto"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en uploadPhoto: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<PatientResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    suspend fun deletePhoto(call: ApplicationCall, patientId: Int) {
        try {
            // Verificar que el paciente existe
            val existingPatient = getPatientByIdUseCase.invoke(patientId).getOrNull()
                ?: run {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ApiResponse<PatientResponse>(success = false, message = "La mascota no existe")
                    )
                    return
                }
            
            if (existingPatient.photoUrl == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiResponse<PatientResponse>(success = false, message = "La mascota no tiene foto para eliminar")
                )
                return
            }
            
            // Eliminar foto de Cloudinary
            val deleted = cloudinaryService.deleteImage(existingPatient.photoUrl)
            
            if (!deleted) {
                println("Advertencia: No se pudo eliminar la imagen de Cloudinary, pero se actualizará el registro")
            }
            
            // Actualizar el paciente eliminando la URL
            val result = updatePatientUseCase.invoke(
                patientId = patientId,
                name = existingPatient.name,
                species = existingPatient.species,
                breed = existingPatient.breed,
                birthDate = existingPatient.birthDate,
                gender = existingPatient.gender,
                weight = existingPatient.weight,
                photoUrl = null
            )
            
            result.fold(
                onSuccess = { patient ->
                    val patientResponse = patientToResponse(patient)
                    call.respond(
                        HttpStatusCode.OK,
                        ApiResponse(
                            success = true,
                            data = patientResponse,
                            message = "Foto eliminada exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ApiResponse<PatientResponse>(
                            success = false,
                            message = error.message ?: "Error al eliminar la foto"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en deletePhoto: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<PatientResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud"
                )
            )
        }
    }
    
    private fun patientToResponse(patient: Patient): PatientResponse {
        return PatientResponse(
            id = patient.id,
            clientId = patient.clientId,
            name = patient.name,
            species = patient.species,
            breed = patient.breed,
            birthDate = patient.birthDate?.toString(),
            gender = patient.gender,
            weight = patient.weight,
            photoUrl = patient.photoUrl,
            createdAt = patient.createdAt.epochSecond * 1000,
            updatedAt = patient.updatedAt.epochSecond * 1000
        )
    }
}

