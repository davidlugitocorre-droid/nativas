package com.example.ecomers.data.repository

import com.example.ecomers.data.remote.api.ApiService
import com.example.ecomers.data.remote.model.RegisterRequest
import com.example.ecomers.domain.model.User
import com.example.ecomers.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de Capa de Datos: UserRepositoryImpl
 * 
 * Implementa la interfaz UserRepository utilizando el cliente de red ApiService
 * para interactuar con la base de datos PostgreSQL remota.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> {
        return try {
            val response = apiService.getAllUsers()
            val domainUsers = response.map { it.toDomain() }
            Result.success(domainUsers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createUser(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Result<User> {
        return try {
            val response = apiService.createUser(
                RegisterRequest(email, password, fullName, role)
            )
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserDetails(userId: Int): Result<User> {
        return try {
            val response = apiService.getUserById(userId)
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUser(
        userId: Int,
        email: String,
        fullName: String,
        role: String
    ): Result<User> {
        return try {
            val response = apiService.updateUser(
                userId,
                RegisterRequest(email, "DUMMY_PWD", fullName, role) // La contraseña se ignora en actualización en el backend
            )
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteUser(userId: Int): Result<String> {
        return try {
            val response = apiService.deleteUser(userId)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
