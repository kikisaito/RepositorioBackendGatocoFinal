package com.example

import io.ktor.http.HttpMethod
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("Authorization")
        exposeHeader("Authorization")
        
        // Permitir Angular localhost y 127.0.0.1
        allowHost("localhost:4200")
        allowHost("127.0.0.1:4200")
        
        allowCredentials = true
        maxAgeInSeconds = 3600
    }
}

