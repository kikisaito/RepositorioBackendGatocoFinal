package com.example.appointments.infrastructure.configuration

import com.example.appointments.application.usecases.CreateAppointmentUseCase
import com.example.appointments.application.usecases.GetAppointmentsByClientUseCase
import com.example.appointments.application.usecases.GetAppointmentsByVeterinarianUseCase
import com.example.appointments.application.usecases.UpdateAppointmentStatusUseCase
import com.example.appointments.application.usecases.UpdateAppointmentUseCase
import com.example.appointments.domain.ports.AppointmentRepository
import com.example.appointments.infrastructure.adapters.http.controllers.AppointmentController
import com.example.appointments.infrastructure.adapters.persistence.PostgreSQLAppointmentRepository
import com.example.auth.domain.ports.ClientRepository
import com.example.patients.domain.ports.PatientRepository
import com.example.services.domain.ports.ServiceTypeRepository
import com.example.auth.domain.ports.VeterinarianRepository
import org.koin.dsl.module

val appointmentModule = module {
    // Repository
    single<AppointmentRepository> { PostgreSQLAppointmentRepository() }
    
    // Use Cases
    single { CreateAppointmentUseCase(get()) }
    single { GetAppointmentsByClientUseCase(get()) }
    single { GetAppointmentsByVeterinarianUseCase(get()) }
    single { UpdateAppointmentStatusUseCase(get()) }
    single { UpdateAppointmentUseCase(get()) }
    
    // Controller
    single {
        AppointmentController(
            createAppointmentUseCase = get(),
            getAppointmentsByClientUseCase = get(),
            getAppointmentsByVeterinarianUseCase = get(),
            updateAppointmentStatusUseCase = get(),
            updateAppointmentUseCase = get(),
            appointmentRepository = get<AppointmentRepository>(),
            clientRepository = get(),
            patientRepository = get(),
            serviceTypeRepository = get(),
            veterinarianRepository = get()
        )
    }
}


