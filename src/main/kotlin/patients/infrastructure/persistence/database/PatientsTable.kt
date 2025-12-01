package com.example.patients.infrastructure.persistence.database

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

/**
 * PatientsTable
 * Definición de la tabla patients en Exposed
 */
object PatientsTable : IntIdTable("patients") {
    val clientId = integer("client_id").references(com.example.auth.infrastructure.persistence.database.ClientsTable.id)
    val name = varchar("name", 255)
    val species = varchar("species", 50)
    val breed = varchar("breed", 50).nullable()
    val birthDate = date("birth_date").nullable()  // Cambiado a date
    val gender = varchar("gender", 10).nullable()
    val weight = decimal("weight", 5, 2).nullable()
    val photoUrl = text("photo_url").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}

