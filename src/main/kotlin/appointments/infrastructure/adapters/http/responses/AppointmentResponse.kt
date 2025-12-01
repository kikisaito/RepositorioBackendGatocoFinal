package com.example.appointments.infrastructure.adapters.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class AppointmentResponse(
    val id: Int,
    val mascotaId: Int,
    val mascota: String,
    val mascotaFoto: String? = null, // Foto de la mascota
    val servicioId: Int,
    val servicio: String,
    val veterinarioId: Int,
    val veterinario: String,
    val clienteId: Int,
    val cliente: String,
    val fecha: String,
    val hora: String,
    val estado: String,
    val notas: String? = null,
    val createdAt: Long? = null
)









