package com.example.ecomers.data.repository

import com.example.ecomers.data.local.pref.SecurityPreferences
import com.example.ecomers.data.remote.api.ApiService
import com.example.ecomers.data.remote.model.LoginRequest
import com.example.ecomers.data.remote.model.RecoverRequest
import com.example.ecomers.data.remote.model.RegisterRequest
import com.example.ecomers.domain.model.User
import com.example.ecomers.domain.model.UserRole
import com.example.ecomers.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de Capa de Datos: AuthRepositoryImpl
 * 
 * Se encarga de orquestar la comunicación entre la API REST (PostgreSQL remota)
 * y el almacenamiento cifrado local (SecurityPreferences).
 * 
 * Implementa la interfaz de Dominio para mantener las capas acopladas mediante abstracciones.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val securityPrefs: SecurityPreferences
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Result<User> {
        return try {
            val response = apiService.register(
                RegisterRequest(email, password, fullName, role)
            )
            Result.success(response.user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<User> {
        return try {
            val response = apiService.login(
                LoginRequest(email, password)
            )
            
            // Persistir token JWT y datos de usuario de forma encriptada
            securityPrefs.saveToken(response.token)
            securityPrefs.saveUserData(
                userId = response.user.id,
                name = response.user.fullName,
                email = response.user.email,
                role = response.user.rol
            )
            
            // Si tiene activado biometría, guardar credenciales para bypass
            if (securityPrefs.isBiometricsEnabled()) {
                securityPrefs.saveEncryptedCredentials(password)
            }

            Result.success(response.user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recoverPassword(email: String): Result<String> {
        return try {
            val response = apiService.recoverPassword(RecoverRequest(email))
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(response.message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        securityPrefs.clearSession()
    }

    override fun getSessionToken(): String? {
        return securityPrefs.getToken()
    }

    override fun getLoggedUser(): User? {
        val id = securityPrefs.getUserId()
        val email = securityPrefs.getUserEmail()
        val name = securityPrefs.getUserName()
        val role = securityPrefs.getUserRole()

        if (id == -1 || email == null || name == null || role == null) {
            return null
        }

        return User(
            id = id,
            email = email,
            fullName = name,
            rol = UserRole.fromString(role)
        )
    }

    override fun isBiometricsEnabled(): Boolean {
        return securityPrefs.isBiometricsEnabled()
    }

    override fun setBiometricsEnabled(enabled: Boolean) {
        securityPrefs.setBiometricsEnabled(enabled)
    }

    override fun saveEncryptedCredentials(password: String) {
        securityPrefs.saveEncryptedCredentials(password)
    }

    override fun getEncryptedCredentials(): String? {
        return securityPrefs.getEncryptedCredentials()
    }
}
