package com.example.ecomers.data.remote.model

import com.google.gson.annotations.SerializedName
import com.example.ecomers.domain.model.Product

/**
 * Representación de Producto en Red (ProductDTO)
 * Estructura DTO recibida de los endpoints REST API que consumen PostgreSQL.
 */
data class ProductDto(
    @SerializedName("id") val id: Int,
    @SerializedName("seller_id") val sellerId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("seller_name") val sellerName: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
) {
    /**
     * Convierte este DTO en una entidad de Dominio Product
     */
    fun toDomain(): Product {
        return Product(
            id = id,
            sellerId = sellerId,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            stock = stock,
            imagenUrl = imagenUrl,
            sellerName = sellerName ?: "Vendedor #${sellerId}",
            createdAt = createdAt
        )
    }
}

/**
 * Petición para Crear o Modificar un Producto
 */
data class ProductCreateRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: Double,
    @SerializedName("stock") val stock: Int,
    @SerializedName("imagenUrl") val imagenUrl: String?
)

/**
 * Respuesta para la Subida de Imagen
 */
data class ImageUploadResponse(
    @SerializedName("imageUrl") val imageUrl: String
)
