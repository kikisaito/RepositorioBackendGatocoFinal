package com.example.services.infrastructure.persistence.database

import org.jetbrains.exposed.dao.id.IntIdTable

/**
 * ServiceTypesTable
 * Definición de la tabla service_types en Exposed
 */
object ServiceTypesTable : IntIdTable("service_types") {
    val name = varchar("name", 255).uniqueIndex()
}


