package com.example.ecomers.data.remote.model

import com.google.gson.annotations.SerializedName
import com.example.ecomers.domain.model.User
import com.example.ecomers.domain.model.UserRole

/**
 * Petición de Registro de Usuario
 * Envia los datos necesarios para insertar un nuevo usuario en la base de datos PostgreSQL.
 */
data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("rol") val rol: String
)

/**
 * Petición de Inicio de Sesión
 * Envía las credenciales tradicionales para verificar el hash Bcrypt en PostgreSQL y generar el JWT.
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

/**
 * Representación de Usuario en Red (UserDTO)
 * Facilita el mapeo seguro entre la respuesta JSON del servidor y las entidades del dominio de la app.
 */
data class UserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("created_at") val createdAt: String? = null
) {
    /**
     * Mapea este DTO a la entidad pura de Dominio User.
     */
    fun toDomain(): User {
        return User(
            id = id,
            email = email,
            fullName = fullName,
            rol = UserRole.fromString(rol),
            createdAt = createdAt
        )
    }
}

/**
 * Respuesta del Servidor para Autenticación Exitosa
 * Retorna el token JWT generado por el servidor y el usuario correspondiente.
 */
data class AuthResponse(
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

/**
 * Petición de Recuperación de Contraseña
 */
data class RecoverRequest(
    @SerializedName("email") val email: String
)

/**
 * Respuesta Genérica de Mensaje del Servidor
 */
data class MessageResponse(
    @SerializedName("message") val message: String,
    @SerializedName("error") val error: String? = null
)
