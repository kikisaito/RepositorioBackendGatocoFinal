package com.example.users.domain.models

import com.example.users.domain.models.valueobjects.Email
import java.time.Instant

data class User(
    val id: Int,
    val role: Boolean, // false = cliente, true = veterinario
    val email: Email,
    val password: String, // Hasheada
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

