package com.example.ecomers.domain.repository

import com.example.ecomers.domain.model.User

/**
 * Contrato de Dominio: AuthRepository
 * 
 * Define la interfaz para todas las interacciones relacionadas con la seguridad,
 * registro, inicio de sesión (JWT) y el Módulo 2 (Autenticación Biométrica).
 * 
 * Por estar en la capa de Dominio, no conoce detalles de librerías como Retrofit o
 * EncryptedSharedPreferences, lo que garantiza la testabilidad pura.
 */
interface AuthRepository {

    /**
     * Registra un usuario nuevo en PostgreSQL.
     */
    suspend fun register(
        email: String, 
        password: String, 
        fullName: String, 
        role: String
    ): Result<User>

    /**
     * Inicia sesión validando credenciales y guardando el Token JWT de manera segura.
     */
    suspend fun login(
        email: String, 
        password: String
    ): Result<User>

    /**
     * Solicita recuperar la contraseña por correo.
     */
    suspend fun recoverPassword(
        email: String
    ): Result<String>

    /**
     * Cierra la sesión activa limpiando los tokens y preferencias.
     */
    fun logout()

    /**
     * Obtiene el token JWT guardado localmente en el dispositivo.
     */
    fun getSessionToken(): String?

    /**
     * Obtiene el usuario actualmente autenticado desde la sesión local.
     */
    fun getLoggedUser(): User?

    /**
     * Verifica si el usuario ha habilitado previamente la huella dactilar (Biometría).
     */
    fun isBiometricsEnabled(): Boolean

    /**
     * Activa o desactiva el inicio biométrico rápido.
     */
    fun setBiometricsEnabled(enabled: Boolean)

    /**
     * Almacena las credenciales de forma cifrada para posibilitar el bypass del Login.
     */
    fun saveEncryptedCredentials(password: String)

    /**
     * Recupera las credenciales guardadas cifradamente para realizar el bypass del Login.
     */
    fun getEncryptedCredentials(): String?
}
