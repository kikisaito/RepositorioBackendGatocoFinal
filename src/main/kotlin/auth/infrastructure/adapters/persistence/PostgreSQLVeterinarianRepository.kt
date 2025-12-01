package com.example.auth.infrastructure.adapters.persistence

import com.example.auth.domain.models.Veterinarian
import com.example.auth.domain.ports.VeterinarianRepository
import com.example.auth.infrastructure.persistence.database.VeterinariansTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import kotlinx.datetime.*

class PostgreSQLVeterinarianRepository : VeterinarianRepository {
    
    override suspend fun findById(id: Int): Veterinarian? = transaction {
        VeterinariansTable.selectAll()
            .where { VeterinariansTable.id eq id }
            .singleOrNull()
            ?.let { rowToVeterinarian(it) }
    }
    
    override suspend fun findByUserId(userId: Int): Veterinarian? = transaction {
        VeterinariansTable.selectAll()
            .where { VeterinariansTable.userId eq userId }
            .singleOrNull()
            ?.let { rowToVeterinarian(it) }
    }
    
    override suspend fun findAll(): List<Veterinarian> = transaction {
        VeterinariansTable.selectAll()
            .map { rowToVeterinarian(it) }
    }
    
    override suspend fun save(veterinarian: Veterinarian): Veterinarian = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        val id = VeterinariansTable.insertAndGetId {
            it[userId] = veterinarian.userId
            it[fullName] = veterinarian.fullName
            it[phone] = veterinarian.phone
            it[createdAt] = nowLocalDateTime
            it[updatedAt] = nowLocalDateTime
        }.value
        
        veterinarian.copy(id = id, createdAt = now, updatedAt = now)
    }
    
    override suspend fun update(veterinarian: Veterinarian): Veterinarian = transaction {
        val now = Instant.now()
        val nowLocalDateTime = now.toKotlinInstant().toLocalDateTime(TimeZone.UTC)
        VeterinariansTable.update({ VeterinariansTable.id eq veterinarian.id }) {
            it[fullName] = veterinarian.fullName
            it[phone] = veterinarian.phone
            it[updatedAt] = nowLocalDateTime
        }
        veterinarian.copy(updatedAt = now)
    }
    
    override suspend fun delete(id: Int): Boolean = transaction {
        VeterinariansTable.deleteWhere { VeterinariansTable.id eq id } > 0
    }
    
    private fun rowToVeterinarian(row: ResultRow): Veterinarian {
        val createdAtLDT = row[VeterinariansTable.createdAt]
        val updatedAtLDT = row[VeterinariansTable.updatedAt]
        val createdAt = createdAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        val updatedAt = updatedAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        return Veterinarian(
            id = row[VeterinariansTable.id].value,
            userId = row[VeterinariansTable.userId],
            fullName = row[VeterinariansTable.fullName],
            phone = row[VeterinariansTable.phone],
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun java.time.Instant.toKotlinInstant(): kotlinx.datetime.Instant =
        kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilli())
    
    private fun kotlinx.datetime.Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds())
}

