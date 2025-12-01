package com.example

import com.example.auth.infrastructure.adapters.http.controllers.AuthController
import com.example.auth.infrastructure.adapters.http.routes.authRoutes
import com.example.patients.infrastructure.adapters.http.controllers.PatientController
import com.example.patients.infrastructure.adapters.http.routes.patientRoutes
import com.example.services.infrastructure.adapters.http.controllers.ServiceTypeController
import com.example.services.infrastructure.adapters.http.routes.serviceTypeRoutes
import com.example.appointments.infrastructure.adapters.http.controllers.AppointmentController
import com.example.appointments.infrastructure.adapters.http.routes.appointmentRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val authController: AuthController by inject()
    val patientController: PatientController by inject()
    val serviceTypeController: ServiceTypeController by inject()
    val appointmentController: AppointmentController by inject()
    
    routing {
        get("/") {
            call.respondText("Gatoco API - Sistema de gestión veterinaria")
        }
        
        // Auth routes with Koin Dependency Injection
        authRoutes(authController)
        
        // Patient routes with Koin Dependency Injection
        patientRoutes(patientController)
        
        // Service Type routes with Koin Dependency Injection
        serviceTypeRoutes(serviceTypeController)
        
        // Appointment routes with Koin Dependency Injection
        appointmentRoutes(appointmentController)
    }
}
