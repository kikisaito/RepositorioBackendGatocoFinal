package com.example.users.domain.models.valueobjects

import com.example.users.domain.exceptions.InvalidUserException

/**
 * Email Value Object
 * Reglas de negocio:
 * - Solo permite dominios: hotmail.com, gmail.com, outlook.com
 * - Debe contener @
 * - Debe tener formato válido
 */
@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { throw InvalidUserException("El email no puede estar vacío") }
        
        val allowedDomains = listOf("hotmail.com", "gmail.com", "outlook.com")
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        
        // Validar formato básico
        if (!emailRegex.matches(value)) {
            throw InvalidUserException("El email no tiene un formato válido")
        }
        
        // Extraer dominio
        val domain = value.substringAfter("@")
        
        // Validar que el dominio esté permitido
        if (!allowedDomains.contains(domain)) {
            throw InvalidUserException(
                "Solo se permiten correos de los siguientes dominios: ${allowedDomains.joinToString(", ")}"
            )
        }
    }
    
    /**
     * Normaliza el email (lowercase y trim)
     */
    fun normalize(): Email = Email(value.lowercase().trim())
    
    override fun toString(): String = value
}
