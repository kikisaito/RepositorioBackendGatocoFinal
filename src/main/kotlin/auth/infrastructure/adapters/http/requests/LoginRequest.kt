package com.example.auth.infrastructure.adapters.http.requests

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

