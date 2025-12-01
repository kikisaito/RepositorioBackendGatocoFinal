package com.example.auth.domain.ports

import com.example.auth.domain.models.Veterinarian

interface VeterinarianRepository {
    suspend fun findById(id: Int): Veterinarian?
    suspend fun findByUserId(userId: Int): Veterinarian?
    suspend fun findAll(): List<Veterinarian>
    suspend fun save(veterinarian: Veterinarian): Veterinarian
    suspend fun update(veterinarian: Veterinarian): Veterinarian
    suspend fun delete(id: Int): Boolean
}

