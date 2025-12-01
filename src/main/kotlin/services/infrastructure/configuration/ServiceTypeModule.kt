package com.example.services.infrastructure.configuration

import com.example.services.application.usecases.GetAllServiceTypesUseCase
import com.example.services.domain.ports.ServiceTypeRepository
import com.example.services.infrastructure.adapters.http.controllers.ServiceTypeController
import com.example.services.infrastructure.adapters.persistence.PostgreSQLServiceTypeRepository
import org.koin.dsl.module

val serviceTypeModule = module {
    // Repository
    single<ServiceTypeRepository> { PostgreSQLServiceTypeRepository() }
    
    // Use Cases
    single { GetAllServiceTypesUseCase(get()) }
    
    // Controller
    single {
        ServiceTypeController(
            getAllServiceTypesUseCase = get()
        )
    }
}


