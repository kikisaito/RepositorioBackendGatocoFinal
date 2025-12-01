package com.example.auth.infrastructure.adapters.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class AuthClientResponse(
    val token: String,
    val clientId: Int,
    val userId: Int,
    val email: String,
    val fullName: String,
    val phone: String?,
    val address: String?
)

