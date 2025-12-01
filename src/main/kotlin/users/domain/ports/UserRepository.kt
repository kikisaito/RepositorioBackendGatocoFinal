package com.example.users.domain.ports

import com.example.users.domain.models.User

interface UserRepository {
    suspend fun findById(id: Int): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findAll(): List<User>
    suspend fun save(user: User): User
    suspend fun update(user: User): User
    suspend fun delete(id: Int): Boolean
}

