package com.example.auth.infrastructure.adapters.http.controllers

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.auth.application.usecases.LoginClientUseCase
import com.example.auth.application.usecases.RegisterClientUseCase
import com.example.auth.domain.exceptions.InvalidUserException
import com.example.auth.domain.models.Client
import com.example.auth.domain.models.Veterinarian
import com.example.auth.infrastructure.adapters.http.requests.LoginRequest
import com.example.auth.infrastructure.adapters.http.requests.RegisterClientRequest
import com.example.auth.infrastructure.adapters.http.responses.AuthClientResponse
import com.example.users.domain.models.User
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*

@Serializable
data class UserResponse(
    val id: Int,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String,
    val createdAt: Long,
    val lastLogin: Long? = null,
    val isActive: Boolean
)

@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val user: T? = null,
    val token: String? = null
)

@Serializable
data class VeterinarianResponse(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val phone: String?
)

class AuthController(
    private val registerClientUseCase: RegisterClientUseCase,
    private val loginClientUseCase: LoginClientUseCase,
    private val veterinarianRepository: com.example.auth.domain.ports.VeterinarianRepository
) {
    private val jwtSecret = "your-256-bit-secret"
    private val jwtIssuer = "https://gatoco-api.com"
    private val jwtAudience = "gatoco-api-audience"
    private val jwtRealm = "Gatoco API"
    
    private fun generateToken(userId: Int, email: String, role: Boolean): String {
        return JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtIssuer)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24 horas
            .sign(Algorithm.HMAC256(jwtSecret))
    }
    
    suspend fun registerClient(call: ApplicationCall) {
        try {
            val request = call.receive<RegisterClientRequest>()
            val result = registerClientUseCase.invoke(
                email = request.email,
                password = request.password,
                fullName = request.fullName,
                phone = request.phone,
                role = request.role
            )
            
            result.fold(
                onSuccess = { result ->
                    val pair = result as Pair<*, *>
                    val user = pair.first as User
                    val userInfo = pair.second
                    
                    val token = generateToken(user.id, user.email.value, user.role)
                    
                    // Extraer información según el tipo de usuario
                    val fullName: String
                    val phone: String?
                    
                    when (userInfo) {
                        is Client -> {
                            fullName = userInfo.fullName
                            phone = userInfo.phone
                        }
                        is Veterinarian -> {
                            fullName = userInfo.fullName
                            phone = userInfo.phone
                        }
                        else -> {
                            throw Exception("Tipo de usuario desconocido")
                        }
                    }
                    
                    // Convertir a formato que espera Angular
                    val userResponse = UserResponse(
                        id = user.id,
                        fullName = fullName,
                        email = user.email.value,
                        phone = phone ?: "",
                        role = if (user.role) "veterinario" else "cliente",
                        createdAt = user.createdAt.epochSecond * 1000,
                        lastLogin = null,
                        isActive = true
                    )
                    
                    call.respond(
                        HttpStatusCode.Created,
                        ApiResponse(
                            success = true,
                            user = userResponse,
                            token = token,
                            message = "Usuario registrado exitosamente"
                        )
                    )
                },
                onFailure = { error ->
                    val statusCode = when (error) {
                        is InvalidUserException -> HttpStatusCode.BadRequest
                        else -> HttpStatusCode.InternalServerError
                    }
                    
                    call.respond(
                        statusCode,
                        ApiResponse<UserResponse>(
                            success = false,
                            message = error.message ?: "Error desconocido"
                        )
                    )
                }
            )
        } catch (e: Exception) {
            println("Error en registerClient: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<UserResponse>(
                    success = false,
                    message = e.message ?: "Error al procesar la solicitud: ${e.javaClass.simpleName}"
                )
            )
        }
    }
    
    suspend fun loginClient(call: ApplicationCall) {
        val request = call.receive<LoginRequest>()
        val result = loginClientUseCase.invoke(
            email = request.email,
            password = request.password
        )
        
        result.fold(
            onSuccess = { result ->
                val pair = result as Pair<*, *>
                val user = pair.first as User
                val userInfo = pair.second
                
                val token = generateToken(user.id, user.email.value, user.role)
                
                // Extraer información según el tipo de usuario
                val fullName: String
                val phone: String?
                
                when (userInfo) {
                    is Client -> {
                        fullName = userInfo.fullName
                        phone = userInfo.phone
                    }
                    is Veterinarian -> {
                        fullName = userInfo.fullName
                        phone = userInfo.phone
                    }
                    else -> {
                        throw Exception("Tipo de usuario desconocido")
                    }
                }
                
                // Convertir a formato que espera Angular
                val userResponse = UserResponse(
                    id = user.id,
                    fullName = fullName,
                    email = user.email.value,
                    phone = phone ?: "",
                    role = if (user.role) "veterinario" else "cliente",
                    createdAt = user.createdAt.epochSecond * 1000,
                    lastLogin = null,
                    isActive = true
                )
                
                call.respond(
                    HttpStatusCode.OK,
                    ApiResponse(
                        success = true,
                        user = userResponse,
                        token = token,
                        message = "Login exitoso"
                    )
                )
            },
            onFailure = { error ->
                val statusCode = when (error) {
                    is InvalidUserException -> HttpStatusCode.Unauthorized
                    else -> HttpStatusCode.InternalServerError
                }
                
                call.respond(
                    statusCode,
                    ApiResponse<UserResponse>(
                        success = false,
                        message = error.message ?: "Error desconocido"
                    )
                )
            }
        )
    }
    
    suspend fun logout(call: ApplicationCall) {
        // TODO: Agregar blacklist de tokens si es necesario
        // Por ahora, el logout es solo del lado del cliente (eliminar el token)
        call.respond(
            HttpStatusCode.OK,
            ApiResponse<String>(
                success = true,
                message = "Sesión cerrada exitosamente"
            )
        )
    }
    
    suspend fun getAllVeterinarians(call: ApplicationCall) {
        try {
            val veterinarians = veterinarianRepository.findAll()
            
            val veterinariansResponse = veterinarians.map { vet ->
                VeterinarianResponse(
                    id = vet.id,
                    userId = vet.userId,
                    fullName = vet.fullName,
                    phone = vet.phone
                )
            }
            
            call.respond(
                HttpStatusCode.OK,
                ApiResponse(
                    success = true,
                    data = veterinariansResponse,
                    message = "Veterinarios obtenidos exitosamente"
                )
            )
        } catch (e: Exception) {
            println("Error en getAllVeterinarians: ${e.message}")
            e.printStackTrace()
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiResponse<List<VeterinarianResponse>>(
                    success = false,
                    message = e.message ?: "Error al obtener los veterinarios"
                )
            )
        }
    }
}
