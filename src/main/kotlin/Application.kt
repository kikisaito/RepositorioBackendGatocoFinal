package com.example

import com.example.database.DatabaseConfig
import com.example.auth.infrastructure.configuration.userModule
import com.example.patients.infrastructure.configuration.patientModule
import com.example.services.infrastructure.configuration.serviceTypeModule
import com.example.appointments.infrastructure.configuration.appointmentModule
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.koin.ktor.plugin.Koin

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Initialize PostgreSQL database
    DatabaseConfig.init()
    
    // Install Koin for Dependency Injection
    install(Koin) {
        modules(userModule, patientModule, serviceTypeModule, appointmentModule)
    }
    
    configureCORS()
    configureSerialization()
    configureSecurity()
    configureRouting()
    
    // Multipart está incluido en ktor-server-core, no necesita instalación explícita
}
