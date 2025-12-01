package com.example.patients.domain.models

import java.time.Instant
import java.time.LocalDate

/**
 * Patient Domain Model
 * Representa una mascota en el sistema
 */
data class Patient(
    val id: Int,
    val clientId: Int,
    val name: String,
    val species: String,
    val breed: String?,
    val birthDate: LocalDate?,
    val gender: String?,
    val weight: Double?,
    val photoUrl: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)


