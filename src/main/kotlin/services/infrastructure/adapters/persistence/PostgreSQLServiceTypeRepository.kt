package com.example.services.infrastructure.adapters.persistence

import com.example.services.domain.models.ServiceType
import com.example.services.domain.ports.ServiceTypeRepository
import com.example.services.infrastructure.persistence.database.ServiceTypesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PostgreSQLServiceTypeRepository : ServiceTypeRepository {
    
    override suspend fun findAll(): List<ServiceType> = transaction {
        ServiceTypesTable.selectAll()
            .map { rowToServiceType(it) }
    }
    
    override suspend fun findById(id: Int): ServiceType? = transaction {
        ServiceTypesTable.selectAll()
            .where { ServiceTypesTable.id eq id }
            .singleOrNull()
            ?.let { rowToServiceType(it) }
    }
    
    private fun rowToServiceType(row: ResultRow): ServiceType {
        return ServiceType(
            id = row[ServiceTypesTable.id].value,
            name = row[ServiceTypesTable.name]
        )
    }
}


