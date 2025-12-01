package com.example.auth.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterClientRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phone: String? = null,
    val role: String? = "cliente"  // "cliente" o "veterinario"
)

