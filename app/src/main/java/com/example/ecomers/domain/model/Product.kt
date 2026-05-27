package com.example.ecomers.domain.model

/**
 * Entidad de Dominio: Product
 * 
 * Representa un producto físico en venta dentro de la plataforma.
 * Es un modelo puro de Kotlin de la capa de Dominio, libre de anotaciones de frameworks.
 * 
 * Vincula al Vendedor mediante `sellerId` y permite asociar el nombre del vendedor
 * para mostrarlo de manera amigable en el catálogo del Comprador.
 */
data class Product(
    val id: Int,
    val sellerId: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?,
    val sellerName: String? = null,
    val createdAt: String? = null
) {
    /**
     * Lógica de negocio auxiliar para verificar disponibilidad del producto.
     * Retorna verdadero si hay existencias físicas en bodega.
     */
    fun hasStock(): Boolean = stock > 0

    /**
     * Retorna el precio formateado con dos decimales y el símbolo monetario COP/USD.
     */
    fun getFormattedPrice(): String = String.format("$%,.2f", precio)
}
