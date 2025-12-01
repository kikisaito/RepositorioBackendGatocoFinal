package com.example.appointments.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class UpdateAppointmentRequest(
    val diagnostico: String? = null,
    val tratamiento: String? = null,
    val estado: String? = null,
    val informacionMascota: InformacionMascotaSnapshot? = null
)

@Serializable
data class InformacionMascotaSnapshot(
    val nombre: String,
    val especie: String,
    val raza: String?,
    val edad: Int?,
    val fechaNacimiento: String?,
    val sexo: String?
)

