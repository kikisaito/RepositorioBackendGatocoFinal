package com.example.patients.infrastructure.adapters.persistence

import com.example.patients.domain.models.Patient
import com.example.patients.domain.ports.PatientRepository
import com.example.patients.infrastructure.persistence.database.PatientsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDate
import kotlinx.datetime.*

/**
 * PostgreSQLPatientRepository
 * Implementación del repositorio de pacientes usando Exposed y PostgreSQL
 */
class PostgreSQLPatientRepository : PatientRepository {
    
    override suspend fun findById(id: Int): Patient? = transaction {
        PatientsTable.selectAll()
            .where { PatientsTable.id eq id }
            .singleOrNull()
            ?.let { rowToPatient(it) }
    }
    
    override suspend fun findByClientId(clientId: Int): List<Patient> = transaction {
        PatientsTable.selectAll()
            .where { PatientsTable.clientId eq clientId }
            .map { rowToPatient(it) }
    }
    
    override suspend fun findAll(): List<Patient> = transaction {
        PatientsTable.selectAll()
            .map { rowToPatient(it) }
    }
    
    override suspend fun save(patient: Patient): Patient = transaction {
        val now = Instant.now()
        val nowKotlinInstant = now.toKotlinInstant()
        val nowLocalDateTime = nowKotlinInstant.toLocalDateTime(TimeZone.UTC)
        
        // Convertir LocalDate a kotlinx.datetime.LocalDate para Exposed
        val kotlinxLocalDate = patient.birthDate?.let { 
            kotlinx.datetime.LocalDate(it.year, it.month.value, it.dayOfMonth)
        }
        
        val id = PatientsTable.insertAndGetId {
            it[clientId] = patient.clientId
            it[name] = patient.name
            it[species] = patient.species
            it[breed] = patient.breed
            it[birthDate] = kotlinxLocalDate
            it[gender] = patient.gender
            it[weight] = patient.weight?.toBigDecimal()
            it[photoUrl] = patient.photoUrl
            it[createdAt] = nowLocalDateTime
            it[updatedAt] = nowLocalDateTime
        }.value
        
        patient.copy(id = id, createdAt = now, updatedAt = now)
    }
    
    override suspend fun update(patient: Patient): Patient = transaction {
        val now = Instant.now()
        val nowKotlinInstant = now.toKotlinInstant()
        val nowLocalDateTime = nowKotlinInstant.toLocalDateTime(TimeZone.UTC)
        
        // Convertir LocalDate a kotlinx.datetime.LocalDate para Exposed
        val kotlinxLocalDate = patient.birthDate?.let { 
            kotlinx.datetime.LocalDate(it.year, it.month.value, it.dayOfMonth)
        }
        
        PatientsTable.update({ PatientsTable.id eq patient.id }) {
            it[clientId] = patient.clientId
            it[name] = patient.name
            it[species] = patient.species
            it[breed] = patient.breed
            it[birthDate] = kotlinxLocalDate
            it[gender] = patient.gender
            it[weight] = patient.weight?.toBigDecimal()
            it[photoUrl] = patient.photoUrl
            it[updatedAt] = nowLocalDateTime
        }
        
        patient.copy(updatedAt = now)
    }
    
    override suspend fun delete(id: Int): Boolean = transaction {
        PatientsTable.deleteWhere { PatientsTable.id eq id } > 0
    }
    
    private fun rowToPatient(row: ResultRow): Patient {
        // Convertir kotlinx.datetime.LocalDate a java.time.LocalDate
        val kotlinxLocalDate: kotlinx.datetime.LocalDate? = row[PatientsTable.birthDate]
        val birthDate = kotlinxLocalDate?.let { 
            LocalDate.of(it.year, it.monthNumber, it.dayOfMonth)
        }
        
        // Convertir kotlinx.datetime.LocalDateTime a java.time.Instant
        val createdAtLDT = row[PatientsTable.createdAt]
        val updatedAtLDT = row[PatientsTable.updatedAt]
        val createdAt = createdAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        val updatedAt = updatedAtLDT.toInstant(TimeZone.UTC).toJavaInstant()
        
        return Patient(
            id = row[PatientsTable.id].value,
            clientId = row[PatientsTable.clientId],
            name = row[PatientsTable.name],
            species = row[PatientsTable.species],
            breed = row[PatientsTable.breed],
            birthDate = birthDate,
            gender = row[PatientsTable.gender],
            weight = row[PatientsTable.weight]?.toDouble(),
            photoUrl = row[PatientsTable.photoUrl],
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    private fun java.time.Instant.toKotlinInstant(): kotlinx.datetime.Instant =
        kotlinx.datetime.Instant.fromEpochMilliseconds(this.toEpochMilli())
    
    private fun kotlinx.datetime.Instant.toJavaInstant(): java.time.Instant =
        java.time.Instant.ofEpochMilli(this.toEpochMilliseconds())
}

