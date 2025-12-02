package com.example

import io.ktor.http.HttpMethod
import io.ktor.http.HttpHeaders
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        // Métodos permitidos (Verbos HTTP)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        // Cabeceras permitidas
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        exposeHeader(HttpHeaders.Authorization)

        // --- EL CAMBIO IMPORTANTE ---
        // Quitamos "allowHost(localhost)" y ponemos anyHost()
        // Esto permite que tu S3 (y cualquiera) pueda conectarse.
        anyHost()

        // Configuración de credenciales
        allowCredentials = true
        maxAgeInSeconds = 3600
    }
}