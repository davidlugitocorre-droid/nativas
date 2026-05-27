package com.example.ecomers.domain.model

/**
 * Entidad de Dominio: Order
 * 
 * Representa una orden de compra realizada por un Comprador.
 * Vincula la información relacional de PostgreSQL como el estado de pago,
 * geolocalización (coordenadas GPS de entrega), y el detalle de items comprados.
 */
data class Order(
    val id: Int,
    val buyerId: Int,
    val total: Double,
    val estadoPago: PaymentStatus,
    val latitud: Double?,
    val longitud: Double?,
    val idTransaccionEpayco: String?,
    val createdAt: String?,
    val items: List<OrderItem> = emptyList()
) {
    /**
     * Formatea el total de la orden con separadores decimales y símbolo de moneda.
     */
    fun getFormattedTotal(): String = String.format("$%,.2f", total)
}

/**
 * Entidad de Dominio: OrderItem
 * 
 * Detalle individual de un producto dentro de una orden de compra específica.
 * Equivalente a la fila de la tabla relacional 'detalles_orden'.
 */
data class OrderItem(
    val id: Int,
    val productId: Int,
    val nombre: String,
    val imagenUrl: String?,
    val cantidad: Int,
    val precioUnitario: Double
) {
    /**
     * Calcula el subtotal del item multiplicando la cantidad por el precio de compra unitario.
     */
    val subtotal: Double
        get() = precioUnitario * cantidad

    fun getFormattedSubtotal(): String = String.format("$%,.2f", subtotal)
}

/**
 * Estado del Pago de la Orden de Compra
 * 
 * Controla el ciclo de vida del checkout integrado con la pasarela de pagos ePayco.
 */
enum class PaymentStatus(val value: String) {
    PENDIENTE("Pendiente"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado");

    companion object {
        fun fromString(statusStr: String): PaymentStatus {
            return values().find { it.value.equals(statusStr, ignoreCase = true) } ?: PENDIENTE
        }
    }
}
