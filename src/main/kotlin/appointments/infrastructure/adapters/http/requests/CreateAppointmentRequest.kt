package com.example.appointments.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class CreateAppointmentRequest(
    val mascotaId: Int,
    val servicioId: Int,
    val veterinarioId: Int,
    val fecha: String,
    val hora: String,
    val notas: String? = null,
    val clienteId: Int? = null,
    val duenoId: Int? = null
)












