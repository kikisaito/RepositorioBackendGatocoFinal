package com.example.auth.application.usecases

import com.example.auth.domain.exceptions.InvalidUserException
import com.example.auth.domain.models.Client
import com.example.auth.domain.models.Veterinarian
import com.example.users.domain.models.valueobjects.Email
import com.example.auth.domain.ports.ClientRepository
import com.example.auth.domain.ports.VeterinarianRepository
import com.example.users.domain.models.User
import com.example.users.domain.ports.UserRepository
import org.mindrot.jbcrypt.BCrypt

class LoginClientUseCase(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val veterinarianRepository: VeterinarianRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Any> {
        return try {
            // Validar formato de email
            val emailVO = Email(email)
            
            // Buscar usuario usando el email validado
            val user = userRepository.findByEmail(emailVO.value)
                ?: return Result.failure(InvalidUserException("Usuario con email $email no encontrado"))
            
            // Verificar password
            if (!BCrypt.checkpw(password, user.password)) {
                return Result.failure(InvalidUserException("Contraseña incorrecta"))
            }
            
            // Buscar información del usuario en la tabla correspondiente según el rol
            return if (user.role) {
                // Es veterinario
                val veterinarian = veterinarianRepository.findByUserId(user.id)
                    ?: return Result.failure(InvalidUserException("No se encontró información del veterinario"))
                Result.success(Pair(user, veterinarian))
            } else {
                // Es cliente
                val client = clientRepository.findByUserId(user.id)
                    ?: return Result.failure(InvalidUserException("No se encontró información del cliente"))
                Result.success(Pair(user, client))
            }
        } catch (e: InvalidUserException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(InvalidUserException("Error al iniciar sesión: ${e.message}"))
        }
    }
}

