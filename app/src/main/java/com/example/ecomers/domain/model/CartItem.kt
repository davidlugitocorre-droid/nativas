package com.example.ecomers.domain.model

/**
 * Entidad de Dominio: CartItem
 * 
 * Representa una entrada dentro del Carrito de Compras local del Comprador.
 * Vincula un producto específico con la cantidad solicitada por el usuario.
 */
data class CartItem(
    val product: Product,
    val cantidad: Int
) {
    /**
     * Lógica de negocio para calcular el subtotal acumulado por este producto
     * multiplicando el precio unitario por la cantidad de unidades deseadas.
     */
    val subtotal: Double
        get() = product.precio * cantidad

    /**
     * Formatea el subtotal de manera amigable para su visualización.
     */
    fun getFormattedSubtotal(): String = String.format("$%,.2f", subtotal)
}
