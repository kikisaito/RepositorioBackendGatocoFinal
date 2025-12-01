package com.example.patients.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePatientRequest(
    val nombre: String? = null,
    val especie: String? = null,
    val raza: String? = null,
    val fechaNacimiento: String? = null,
    val sexo: String? = null,
    val peso: Double? = null
)

