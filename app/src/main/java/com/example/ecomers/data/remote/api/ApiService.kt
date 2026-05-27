package com.example.ecomers.data.remote.api

import com.example.ecomers.data.remote.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * Cliente REST: ApiService
 * 
 * Interfaz que define todos los endpoints expuestos por nuestro backend Node.js
 * conectado a la base de datos PostgreSQL de Render.
 * 
 * Se comunica de manera asíncrona mediante corrutinas de Kotlin (`suspend`).
 * La inyección del Token JWT se realiza de manera centralizada e invisible en el
 * OkHttpClient mediante un interceptor de red.
 */
interface ApiService {

    // ==========================================
    // 1. ENDPOINTS DE AUTENTICACIÓN
    // ==========================================

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @POST("api/auth/recover")
    suspend fun recoverPassword(
        @Body request: RecoverRequest
    ): MessageResponse

    // ==========================================
    // 2. ENDPOINTS DE ADMINISTRADOR (CRUD Usuarios)
    // ==========================================

    @GET("api/users")
    suspend fun getAllUsers(): List<UserDto>

    @POST("api/users")
    suspend fun createUser(
        @Body request: RegisterRequest
    ): UserDto

    @GET("api/users/{id}")
    suspend fun getUserById(
        @Path("id") id: Int
    ): UserDto

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body request: RegisterRequest
    ): UserDto

    @DELETE("api/users/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int
    ): MessageResponse

    // ==========================================
    // 3. ENDPOINTS DE PRODUCTOS (Vendedor y Comprador)
    // ==========================================

    @GET("api/products")
    suspend fun getCatalogProducts(): List<ProductDto>

    @GET("api/products/seller/{sellerId}")
    suspend fun getSellerProducts(
        @Path("sellerId") sellerId: Int
    ): List<ProductDto>

    @POST("api/products")
    suspend fun createProduct(
        @Body request: ProductCreateRequest
    ): ProductDto

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: ProductCreateRequest
    ): ProductDto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Path("id") id: Int
    ): MessageResponse

    @Multipart
    @POST("api/products/upload")
    suspend fun uploadProductImage(
        @Part image: MultipartBody.Part
    ): ImageUploadResponse

    // ==========================================
    // 4. ENDPOINTS DE COMPRAS Y ÓRDENES
    // ==========================================

    @POST("api/orders")
    suspend fun createOrder(
        @Body request: OrderCreateRequest
    ): MessageResponse

    @GET("api/orders/buyer/{buyerId}")
    suspend fun getBuyerOrderHistory(
        @Path("buyerId") buyerId: Int
    ): List<OrderDto>

    // ==========================================
    // 5. ENDPOINTS DE PASARELA DE PAGO (ePayco)
    // ==========================================

    @POST("api/orders/{id}/pay")
    suspend fun processOrderPayment(
        @Path("id") orderId: Int,
        @Body request: PaymentRequest
    ): MessageResponse
}