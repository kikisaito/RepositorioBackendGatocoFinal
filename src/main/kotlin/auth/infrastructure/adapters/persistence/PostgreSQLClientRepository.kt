package com.example.auth.infrastructure.adapters.persistence

import com.example.auth.domain.models.Client
import com.example.auth.domain.ports.ClientRepository
import com.example.auth.infrastructure.persistence.database.ClientsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import kotlinx.datetime.*

class PostgreSQLClientRepository : ClientRepository {
    
    override suspend fun findById(id: Int): Client? = transaction {
        ClientsTable.selectAll()
            .where { ClientsTable.id eq id }
            .singleOrNull()
            ?.let { rowToClient(it) }
    }
    
    override suspend fun findByUserId(userId: Int): Client? = transaction {
        ClientsTable.selectAll()
            .where { ClientsTable.userId eq userId }
            .singleOrNull()
            ?.let { rowToClient(it) }
    }
    
    override suspend fun findAll(): List<Client> = transaction {
        ClientsTable.selectAll()
            .map { rowToClient(it) }
    }
    
    override suspend fun save(client: Client): Client = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        val id = ClientsTable.insertAndGetId {
            it[userId] = client.userId
            it[fullName] = client.fullName
            it[phone] = client.phone
            it[createdAt] = nowLocalDateTime
            it[updatedAt] = nowLocalDateTime
        }.value
        
        client.copy(id = id, createdAt = now, updatedAt = now)
    }
    
    override suspend fun update(client: Client): Client = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        ClientsTable.update({ ClientsTable.id eq client.id }) {
            it[fullName] = client.fullName
            it[phone] = client.phone
            it[updatedAt] = nowLocalDateTime
        }
        client.copy(updatedAt = now)
    }
    
    override suspend fun delete(id: Int): Boolean = transaction {
        ClientsTable.deleteWhere { ClientsTable.id eq id } > 0
    }
    
    private fun rowToClient(row: ResultRow): Client {
        val createdAtLDT = row[ClientsTable.createdAt]
        val updatedAtLDT = row[ClientsTable.updatedAt]
        val createdAt = createdAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        val updatedAt = updatedAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        return Client(
            id = row[ClientsTable.id].value,
            userId = row[ClientsTable.userId],
            fullName = row[ClientsTable.fullName],
            phone = row[ClientsTable.phone],
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun java.time.Instant.toKotlinInstant(): kotlinx.datetime.Instant =
        kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilli())
    
    private fun kotlinx.datetime.Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds())
}

