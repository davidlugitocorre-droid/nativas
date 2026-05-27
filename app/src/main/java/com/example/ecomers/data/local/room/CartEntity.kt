package com.example.ecomers.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ecomers.domain.model.CartItem
import com.example.ecomers.domain.model.Product

/**
 * Entidad Room: CartEntity
 * 
 * Representa una fila en la base de datos SQLite local para persistir el carrito
 * de compras temporal del Comprador.
 * 
 * Permite que las compras persistan incluso si la aplicación se cierra inesperadamente
 * o se queda sin conexión de red temporal.
 */
@Entity(tableName = "carrito_compras")
data class CartEntity(
    @PrimaryKey val id: Int, // El ID del producto actúa como clave primaria para evitar duplicaciones
    val sellerId: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?,
    val cantidad: Int
) {
    /**
     * Convierte este registro de base de datos local a un modelo de dominio CartItem.
     */
    fun toDomain(): CartItem {
        return CartItem(
            product = Product(
                id = id,
                sellerId = sellerId,
                nombre = nombre,
                descripcion = descripcion,
                precio = precio,
                stock = stock,
                imagenUrl = imagenUrl
            ),
            cantidad = cantidad
        )
    }

    companion object {
        /**
         * Crea un registro de Room a partir de un CartItem del dominio.
         */
        fun fromDomain(domainItem: CartItem): CartEntity {
            return CartEntity(
                id = domainItem.product.id,
                sellerId = domainItem.product.sellerId,
                nombre = domainItem.product.nombre,
                descripcion = domainItem.product.descripcion,
                precio = domainItem.product.precio,
                stock = domainItem.product.stock,
                imagenUrl = domainItem.product.imagenUrl,
                cantidad = domainItem.cantidad
            )
        }
    }
}
