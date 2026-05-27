package com.example.ecomers.domain.model

/**
 * Entidad de Dominio: User
 * 
 * Representa a un usuario dentro del ecosistema del Marketplace.
 * Este modelo es puro de Kotlin (independiente de librerías de persistencia o red)
 * según los lineamientos de Clean Architecture.
 * 
 * Soporta los tres roles de negocio:
 * 1. ADMIN - Administrador del sistema con acceso completo.
 * 2. SELLER - Vendedor que publica y gestiona productos.
 * 3. BUYER - Comprador que explora el catálogo, gestiona el carrito y realiza compras.
 */
data class User(
    val id: Int,
    val email: String,
    val fullName: String,
    val rol: UserRole,
    val createdAt: String? = null
)

/**
 * Enumeración estricta de los Roles del Negocio
 * 
 * Garantiza la integridad de tipos en la aplicación cliente-servidor,
 * alineándose con las restricciones de la base de datos PostgreSQL.
 */
enum class UserRole(val value: String) {
    ADMIN("admin"),
    SELLER("seller"),
    BUYER("buyer");

    companion object {
        /**
         * Mapea un string de base de datos al enum de Kotlin correspondientemente.
         * Si no coincide, por defecto asigna el rol de comprador por seguridad.
         */
        fun fromString(roleStr: String): UserRole {
            return values().find { it.value.equals(roleStr, ignoreCase = true) } ?: BUYER
        }
    }
}
