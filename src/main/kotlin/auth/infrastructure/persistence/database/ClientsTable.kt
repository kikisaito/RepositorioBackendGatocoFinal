package com.example.auth.infrastructure.persistence.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object ClientsTable : IntIdTable("clients") {
    val userId = integer("user_id").uniqueIndex()
    val fullName = varchar("fullname", 255)
    val phone = varchar("phone", 20).nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

