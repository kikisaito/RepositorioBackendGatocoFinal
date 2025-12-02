package com.example.database

import com.example.auth.infrastructure.persistence.database.ClientsTable
import com.example.auth.infrastructure.persistence.database.VeterinariansTable
import com.example.appointments.infrastructure.persistence.database.AppointmentsTable
import com.example.patients.infrastructure.persistence.database.PatientsTable
import com.example.services.infrastructure.persistence.database.ServiceTypesTable
import com.example.users.infrastructure.persistence.database.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true // No falla si no existe el archivo .env
        directory = "./" // Buscar en la raíz del proyecto
    }
    
    private val dbUrl = dotenv["DB_URL"] ?: System.getenv("DB_URL") ?: "jdbc:postgresql://54.83.45.10/db_gatoOCOs"
    private val dbUser = dotenv["DB_USER"] ?: System.getenv("DB_USER")
    private val dbPassword = dotenv["DB_PASSWORD"] ?: System.getenv("DB_PASSWORD")
    
    fun init() {
        // Configurar connection pool con HikariCP
        val config = HikariConfig().apply {
            jdbcUrl = dbUrl
            username = dbUser
            password = dbPassword
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)
        
        // Crear tablas
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UsersTable, ClientsTable, VeterinariansTable, PatientsTable, ServiceTypesTable, AppointmentsTable)
        }
    }
}

