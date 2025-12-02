```
package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        // En lugar de anyHost(), ponemos TU dirección exacta de S3
        // (Sin la barra / al final)
        allowHost("gatocofrontendangular.s3-website-us-east-1.amazonaws.com", schemes = listOf("http", "https"))
        
        // También dejamos localhost por si quieres probar en tu compu
        allowHost("localhost:4200")
        allowHost("localhost:8080")

        allowMethod(HttpMethod.Options) // ¡CRÍTICO!
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }
}
```