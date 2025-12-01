package com.example.users.infrastructure.persistence.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object UsersTable : IntIdTable("users") {
    val role = bool("role").default(false) // false = cliente, true = veterinario
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

