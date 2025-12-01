package com.example.auth.domain.ports

import com.example.auth.domain.models.Client

interface ClientRepository {
    suspend fun findById(id: Int): Client?
    suspend fun findByUserId(userId: Int): Client?
    suspend fun findAll(): List<Client>
    suspend fun save(client: Client): Client
    suspend fun update(client: Client): Client
    suspend fun delete(id: Int): Boolean
}

