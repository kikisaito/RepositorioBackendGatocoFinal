package com.example.patients.infrastructure.adapters.http.responses

import kotlinx.serialization.Serializable

@Serializable
data class PatientResponse(
    val id: Int,
    val clientId: Int,
    val name: String,
    val species: String,
    val breed: String?,
    val birthDate: String?,
    val gender: String?,
    val weight: Double?,
    val photoUrl: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)


