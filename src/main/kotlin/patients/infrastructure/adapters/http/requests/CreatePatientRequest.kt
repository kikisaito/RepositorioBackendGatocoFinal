package com.example.patients.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreatePatientRequest(
    val nombre: String? = null,
    val especie: String? = null,
    val raza: String? = null,
    val fechaNacimiento: String? = null,
    val sexo: String? = null,
    val peso: Double? = null,
    val duenoId: Int? = null,
    val clientId: Int? = null
)

