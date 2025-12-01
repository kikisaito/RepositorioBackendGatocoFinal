package com.example.patients.domain.ports

import com.example.patients.domain.models.Patient

/**
 * Patient Repository Interface
 * Define las operaciones de acceso a datos para pacientes
 */
interface PatientRepository {
    suspend fun findById(id: Int): Patient?
    suspend fun findByClientId(clientId: Int): List<Patient>
    suspend fun findAll(): List<Patient>
    suspend fun save(patient: Patient): Patient
    suspend fun update(patient: Patient): Patient
    suspend fun delete(id: Int): Boolean
}


