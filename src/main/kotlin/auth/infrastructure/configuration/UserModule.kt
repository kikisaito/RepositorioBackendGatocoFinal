package com.example.auth.infrastructure.configuration

import com.example.auth.application.usecases.LoginClientUseCase
import com.example.auth.application.usecases.RegisterClientUseCase
import com.example.auth.domain.ports.ClientRepository
import com.example.auth.domain.ports.VeterinarianRepository
import com.example.auth.infrastructure.adapters.http.controllers.AuthController
import com.example.auth.infrastructure.adapters.persistence.PostgreSQLClientRepository
import com.example.auth.infrastructure.adapters.persistence.PostgreSQLVeterinarianRepository
import com.example.users.domain.ports.UserRepository
import com.example.users.infrastructure.adapters.persistence.PostgreSQLUserRepository
import org.koin.dsl.module

val userModule = module {
    // Repositories - UserRepository es solo para uso interno (no se expone)
    single<UserRepository> { PostgreSQLUserRepository() }
    single<ClientRepository> { PostgreSQLClientRepository() }
    single<VeterinarianRepository> { PostgreSQLVeterinarianRepository() }
    
    // Use Cases - Solo para entidades de negocio (Client, Veterinarian)
    single { RegisterClientUseCase(get(), get(), get()) }
    single { LoginClientUseCase(get(), get(), get()) }
    
    // Controllers
    single {
        AuthController(
            registerClientUseCase = get(),
            loginClientUseCase = get(),
            veterinarianRepository = get()
        )
    }
}

