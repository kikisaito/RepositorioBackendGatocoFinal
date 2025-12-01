package com.example.auth.application.usecases

import com.example.auth.domain.exceptions.InvalidUserException
import com.example.auth.domain.models.Client
import com.example.auth.domain.models.Veterinarian
import com.example.users.domain.models.valueobjects.Email
import com.example.users.domain.models.valueobjects.Password
import com.example.auth.domain.ports.ClientRepository
import com.example.auth.domain.ports.VeterinarianRepository
import com.example.users.domain.models.User
import com.example.users.domain.ports.UserRepository
import org.mindrot.jbcrypt.BCrypt

class RegisterClientUseCase(
    private val userRepository: UserRepository,
    private val clientRepository: ClientRepository,
    private val veterinarianRepository: VeterinarianRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        fullName: String,
        phone: String? = null,
        role: String? = "cliente" // Por defecto cliente, puede ser "veterinario"
    ): Result<Any> {
        return try {
            // Validaciones mediante Value Objects
            val emailVO = Email(email)
            val passwordVO = Password(password)
            
            // Verificar si el email ya existe
            val existingUser = userRepository.findByEmail(email)
            if (existingUser != null) {
                return Result.failure(InvalidUserException("El email ya está registrado"))
            }
            
            // Hash password
            val hashedPassword = BCrypt.hashpw(passwordVO.value, BCrypt.gensalt())
            
            // Convertir role string a boolean (false = cliente, true = veterinario)
            val userRole = when (role?.lowercase()) {
                "veterinario" -> true
                else -> false // Por defecto cliente
            }
            
            // Crear usuario con el rol especificado
            val user = User(
                id = 0,
                role = userRole,
                email = emailVO,
                password = hashedPassword
            )
            
            val savedUser = userRepository.save(user)
            
            // Crear registro en la tabla correspondiente según el rol
            return if (userRole) {
                // Es veterinario
                val veterinarian = Veterinarian(
                    id = 0,
                    userId = savedUser.id,
                    fullName = fullName,
                    phone = phone
                )
                val savedVeterinarian = veterinarianRepository.save(veterinarian)
                Result.success(Pair(savedUser, savedVeterinarian))
            } else {
                // Es cliente
                val client = Client(
                    id = 0,
                    userId = savedUser.id,
                    fullName = fullName,
                    phone = phone
                )
                val savedClient = clientRepository.save(client)
                Result.success(Pair(savedUser, savedClient))
            }
        } catch (e: InvalidUserException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(InvalidUserException("Error al registrar usuario: ${e.message}"))
        }
    }
}

