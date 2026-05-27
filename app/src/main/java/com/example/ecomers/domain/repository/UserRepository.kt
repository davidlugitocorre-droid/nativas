package com.example.ecomers.domain.repository

import com.example.ecomers.domain.model.User

/**
 * Contrato de Dominio: UserRepository
 * 
 * Define las operaciones requeridas por el Módulo 3 (CRUD de Usuarios por Administrador)
 * en la base de datos PostgreSQL.
 */
interface UserRepository {

    /**
     * Recupera todos los usuarios registrados en el sistema.
     */
    suspend fun getUsers(): Result<List<User>>

    /**
     * Registra un nuevo usuario con rol explícito.
     */
    suspend fun createUser(
        email: String,
        password: String,
        fullName: String,
        role: String
    ): Result<User>

    /**
     * Obtiene el detalle de un usuario específico.
     */
    suspend fun getUserDetails(
        userId: Int
    ): Result<User>

    /**
     * Actualiza los datos de perfil y rol de un usuario existente.
     */
    suspend fun updateUser(
        userId: Int,
        email: String,
        fullName: String,
        role: String
    ): Result<User>

    /**
     * Elimina permanentemente a un usuario de la base de datos PostgreSQL.
     */
    suspend fun deleteUser(
        userId: Int
    ): Result<String>
}
