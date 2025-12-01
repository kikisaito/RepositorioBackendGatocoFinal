val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ApplicationKt"
}

val ktor_version = "3.3.1"

dependencies {
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    
    // CORS
    implementation("io.ktor:ktor-server-cors:$ktor_version")
    
    // JWT
    implementation("com.auth0:java-jwt:4.4.0")
    
    // HikariCP (Connection Pool)
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Koin - Dependency Injection (compatible con Ktor 3.3.1)
    implementation("io.insert-koin:koin-ktor:3.5.0")
    implementation("io.insert-koin:koin-logger-slf4j:3.5.0")
    
    // PostgreSQL & Exposed
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("org.jetbrains.exposed:exposed-core:0.47.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.47.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.47.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.47.0")
    
    // Password Hashing
    implementation("org.mindrot:jbcrypt:0.4")
    
    // Cloudinary for image upload
    // SDK de Cloudinary para Java (compatible con Kotlin)
    // kotlin-uploader requiere cambios en el código, usando la dependencia de Java
    implementation("com.cloudinary:cloudinary-http44:1.37.0")
    
    // Dotenv para leer variables de entorno desde archivo .env
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
    
    // Ktor Utils para readBytes y otras utilidades
    implementation("io.ktor:ktor-utils:$ktor_version")
    
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
