package com.example.auth.infrastructure.adapters.http.routes

import com.example.auth.infrastructure.adapters.http.controllers.AuthController
import io.ktor.server.routing.*

fun Route.authRoutes(controller: AuthController) {
    route("/api/v1/auth") {
        // Register de clientes
        post("/clients/register") {
            controller.registerClient(call)
        }

        // Login de clientes
        post("/clients/login") {
            controller.loginClient(call)
        }
        
        // Logout
        post("/logout") {
            controller.logout(call)
        }
    }
    
    route("/api/v1") {
        // Obtener todos los veterinarios
        get("/veterinarios") {
            controller.getAllVeterinarians(call)
        }
    }
}

