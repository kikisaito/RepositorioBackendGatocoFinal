package com.example.auth.domain.models

import java.time.Instant

data class Veterinarian(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val phone: String?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)

