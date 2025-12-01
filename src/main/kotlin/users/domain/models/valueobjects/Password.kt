package com.example.users.domain.models.valueobjects

import com.example.users.domain.exceptions.InvalidUserException

/**
 * Password Value Object
 * Reglas de negocio:
 * - Mínimo 8 caracteres
 * - Máximo 16 caracteres
 * - Al menos una mayúscula
 * - Al menos un carácter especial
 * - Al menos un número
 * - No puede contener espacios
 */
@JvmInline
value class Password(val value: String) {
    init {
        val password = value.trim()
        
        // Validar longitud mínima
        require(password.length >= 8) {
            throw InvalidUserException("La contraseña debe tener al menos 8 caracteres")
        }
        
        // Validar longitud máxima
        require(password.length <= 16) {
            throw InvalidUserException("La contraseña no puede tener más de 16 caracteres")
        }
        
        // Validar que no contenga espacios
        require(!password.contains(" ")) {
            throw InvalidUserException("La contraseña no puede contener espacios")
        }
        
        // Validar que tenga al menos una mayúscula
        require(password.any { it.isUpperCase() }) {
            throw InvalidUserException("La contraseña debe contener al menos una letra mayúscula")
        }
        
        // Validar que tenga al menos un número
        require(password.any { it.isDigit() }) {
            throw InvalidUserException("La contraseña debe contener al menos un número")
        }
        
        // Validar que tenga al menos un carácter especial
        val specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?"
        require(password.any { it in specialChars }) {
            throw InvalidUserException("La contraseña debe contener al menos un carácter especial (!@#$%^&*()_+-=[]{}|;:,.<>?)")
        }
    }
    
    override fun toString(): String = value
}

