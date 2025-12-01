package com.example.users.infrastructure.adapters.persistence

import com.example.users.domain.models.valueobjects.Email
import com.example.users.domain.models.User
import com.example.users.domain.ports.UserRepository
import com.example.users.infrastructure.persistence.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.datetime.*

class PostgreSQLUserRepository : UserRepository {
    
    override suspend fun findById(id: Int): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.let { rowToUser(it) }
    }
    
    override suspend fun findByEmail(email: String): User? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?.let { rowToUser(it) }
    }
    
    override suspend fun findAll(): List<User> = transaction {
        UsersTable.selectAll()
            .map { rowToUser(it) }
    }
    
    override suspend fun save(user: User): User = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        val id = UsersTable.insertAndGetId {
            it[role] = user.role
            it[email] = user.email.value
            it[password] = user.password
            it[createdAt] = nowLocalDateTime
            it[updatedAt] = nowLocalDateTime
        }.value
        
        user.copy(id = id, createdAt = now, updatedAt = now)
    }
    
    override suspend fun update(user: User): User = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        UsersTable.update({ UsersTable.id eq user.id }) {
            it[role] = user.role
            it[email] = user.email.value
            it[password] = user.password
            it[updatedAt] = nowLocalDateTime
        }
        user.copy(updatedAt = now)
    }
    
    override suspend fun delete(id: Int): Boolean = transaction {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }
    
    private fun rowToUser(row: ResultRow): User {
        val createdAtLDT = row[UsersTable.createdAt]
        val updatedAtLDT = row[UsersTable.updatedAt]
        val createdAt = createdAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        val updatedAt = updatedAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        return User(
            id = row[UsersTable.id].value,
            role = row[UsersTable.role],
            email = Email(row[UsersTable.email]),
            password = row[UsersTable.password],
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun java.time.Instant.toKotlinInstant(): kotlinx.datetime.Instant =
        kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilli())
    
    private fun kotlinx.datetime.Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds())
}

