package com.example

import io.ktor.server.application.*

fun Application.configureSecurity() {
    // JWT authentication configuration is handled in AuthController
    // No global security configuration needed for public endpoints
}
