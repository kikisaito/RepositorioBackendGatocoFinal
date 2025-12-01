package com.example.services.infrastructure.adapters.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class ServiceTypeResponse(
    val id: Int,
    val name: String
)


