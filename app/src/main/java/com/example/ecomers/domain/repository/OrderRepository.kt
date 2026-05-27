package com.example.ecomers.domain.repository

import com.example.ecomers.domain.model.CartItem
import com.example.ecomers.domain.model.Order

/**
 * Contrato de Dominio: OrderRepository
 * 
 * Interfaz que abstrae las operaciones de finalización de compra (Checkout con GPS,
 * Módulo 6), la consulta de órdenes previas (Módulo 5) y el procesamiento de pagos
 * integrados con la pasarela ePayco (Módulo 7).
 */
interface OrderRepository {

    /**
     * Envía la orden finalizada al servidor de PostgreSQL.
     * Recibe los items del carrito, el total y la geolocalización física (latitud/longitud).
     */
    suspend fun createOrder(
        total: Double,
        latitud: Double?,
        longitud: Double?,
        items: List<CartItem>
    ): Result<String>

    /**
     * Consulta el historial de órdenes acumuladas de un Comprador.
     */
    suspend fun getBuyerOrderHistory(
        buyerId: Int
    ): Result<List<Order>>

    /**
     * Procesa la confirmación del pago de ePayco, actualizando la base de datos PostgreSQL.
     */
    suspend fun processPayment(
        orderId: Int,
        transactionId: String,
        status: String
    ): Result<String>
}
