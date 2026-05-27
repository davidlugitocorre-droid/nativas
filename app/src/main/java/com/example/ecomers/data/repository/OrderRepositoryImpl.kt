package com.example.ecomers.data.repository

import com.example.ecomers.data.remote.api.ApiService
import com.example.ecomers.data.remote.model.OrderCreateRequest
import com.example.ecomers.data.remote.model.OrderItemRequest
import com.example.ecomers.data.remote.model.PaymentRequest
import com.example.ecomers.domain.model.CartItem
import com.example.ecomers.domain.model.Order
import com.example.ecomers.domain.repository.OrderRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de Capa de Datos: OrderRepositoryImpl
 * 
 * Implementa el contrato OrderRepository traduciendo las peticiones de compras,
 * coordenadas GPS y pagos ePayco en peticiones DTO para la API REST.
 */
@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : OrderRepository {

    override suspend fun createOrder(
        total: Double,
        latitud: Double?,
        longitud: Double?,
        items: List<CartItem>
    ): Result<String> {
        return try {
            // Mapear los items locales del carrito a la estructura de red esperada por PostgreSQL
            val networkItems = items.map { cartItem ->
                OrderItemRequest(
                    productId = cartItem.product.id,
                    cantidad = cartItem.cantidad,
                    precioUnitario = cartItem.product.precio
                )
            }

            val request = OrderCreateRequest(
                total = total,
                latitud = latitud,
                longitud = longitud,
                items = networkItems
            )

            val response = apiService.createOrder(request)
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(response.message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBuyerOrderHistory(buyerId: Int): Result<List<Order>> {
        return try {
            val response = apiService.getBuyerOrderHistory(buyerId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun processPayment(
        orderId: Int,
        transactionId: String,
        status: String
    ): Result<String> {
        return try {
            val response = apiService.processOrderPayment(
                orderId,
                PaymentRequest(transactionId, status)
            )
            if (response.error != null) {
                Result.failure(Exception(response.error))
            } else {
                Result.success(response.message)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
