package com.example.ecomers.data.remote.model

import com.google.gson.annotations.SerializedName
import com.example.ecomers.domain.model.Order
import com.example.ecomers.domain.model.OrderItem
import com.example.ecomers.domain.model.PaymentStatus

/**
 * Representación de Orden en Red (OrderDTO)
 * Refleja la estructura de la tabla 'ordenes' de PostgreSQL al ser devuelta por la API.
 */
data class OrderDto(
    @SerializedName("id") val id: Int,
    @SerializedName("buyer_id") val buyerId: Int,
    @SerializedName("total") val total: Double,
    @SerializedName("estado_pago") val estadoPago: String,
    @SerializedName("latitud") val latitud: Double?,
    @SerializedName("longitud") val longitud: Double?,
    @SerializedName("id_transaccion_epayco") val idTransaccionEpayco: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("items") val items: List<OrderItemDto>? = null
) {
    /**
     * Mapea este DTO de Orden a la entidad de Dominio Order
     */
    fun toDomain(): Order {
        return Order(
            id = id,
            buyerId = buyerId,
            total = total,
            estadoPago = PaymentStatus.fromString(estadoPago),
            latitud = latitud,
            longitud = longitud,
            idTransaccionEpayco = idTransaccionEpayco,
            createdAt = createdAt,
            items = items?.map { it.toDomain() } ?: emptyList()
        )
    }
}

/**
 * Representación del Detalle de Orden en Red (OrderItemDTO)
 */
data class OrderItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("productId") val productId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("imagenUrl") val imagenUrl: String?,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double
) {
    fun toDomain(): OrderItem {
        return OrderItem(
            id = id,
            productId = productId,
            nombre = nombre,
            imagenUrl = imagenUrl,
            cantidad = cantidad,
            precioUnitario = precioUnitario
        )
    }
}

/**
 * Petición para Crear una Nueva Orden (Checkout)
 * Incluye campos obligatorios de geolocalización (latitud y longitud).
 */
data class OrderCreateRequest(
    @SerializedName("total") val total: Double,
    @SerializedName("latitud") val latitud: Double?,
    @SerializedName("longitud") val longitud: Double?,
    @SerializedName("items") val items: List<OrderItemRequest>
)

/**
 * Entrada de Item Individual en la Petición de Creación de Orden
 */
data class OrderItemRequest(
    @SerializedName("productId") val productId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precioUnitario") val precioUnitario: Double
)

/**
 * Petición para Procesar Pago Simulando ePayco
 */
data class PaymentRequest(
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("status") val status: String // "Aprobado", "Rechazado", "Pendiente"
)
