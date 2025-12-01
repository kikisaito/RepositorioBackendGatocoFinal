package com.example.patients.infrastructure.configuration

import com.example.appointments.domain.ports.AppointmentRepository
import com.example.patients.application.usecases.CreatePatientUseCase
import com.example.patients.application.usecases.GetPatientsByClientUseCase
import com.example.patients.application.usecases.UpdatePatientUseCase
import com.example.patients.application.usecases.DeletePatientUseCase
import com.example.patients.application.usecases.GetPatientByIdUseCase
import com.example.patients.domain.ports.PatientRepository
import com.example.patients.infrastructure.adapters.http.controllers.PatientController
import com.example.patients.infrastructure.adapters.persistence.PostgreSQLPatientRepository
import com.example.patients.infrastructure.services.CloudinaryService
import io.github.cdimascio.dotenv.dotenv
import org.koin.dsl.module

val patientModule = module {
    // Repository
    single<PatientRepository> { PostgreSQLPatientRepository() }
    
    // Cloudinary Service
    single {
        val dotenv = dotenv {
            ignoreIfMissing = true
            directory = "./"
        }
        
        val cloudName = dotenv["CLOUDINARY_CLOUD_NAME"] 
            ?: System.getenv("CLOUDINARY_CLOUD_NAME") 
            ?: throw IllegalStateException("CLOUDINARY_CLOUD_NAME no está configurado")
        val apiKey = dotenv["CLOUDINARY_API_KEY"] 
            ?: System.getenv("CLOUDINARY_API_KEY") 
            ?: throw IllegalStateException("CLOUDINARY_API_KEY no está configurado")
        val apiSecret = dotenv["CLOUDINARY_API_SECRET"] 
            ?: System.getenv("CLOUDINARY_API_SECRET") 
            ?: throw IllegalStateException("CLOUDINARY_API_SECRET no está configurado")
        CloudinaryService(cloudName, apiKey, apiSecret)
    }
    
    // Use Cases
    single { CreatePatientUseCase(get()) }
    single { GetPatientsByClientUseCase(get()) }
    single { UpdatePatientUseCase(get()) }
    single { DeletePatientUseCase(get(), get<AppointmentRepository>()) }
    single { GetPatientByIdUseCase(get()) }
    
    // Controller - ahora también necesita ClientRepository y CloudinaryService
    single {
        PatientController(
            createPatientUseCase = get(),
            getPatientsByClientUseCase = get(),
            clientRepository = get(),  // Koin inyectará automaticamente si está en otro módulo
            updatePatientUseCase = get(),
            deletePatientUseCase = get(),
            getPatientByIdUseCase = get(),
            cloudinaryService = get()
        )
    }
}

