package com.example.ecomers.data.repository

import com.example.ecomers.data.remote.api.ApiService
import com.example.ecomers.data.remote.model.ProductCreateRequest
import com.example.ecomers.domain.model.Product
import com.example.ecomers.domain.repository.ProductRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de Capa de Datos: ProductRepositoryImpl
 * 
 * Orquesta las peticiones REST API de productos interactuando con PostgreSQL
 * y simulando la subida de archivos en la nube de forma ágil y segura.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ProductRepository {

    override suspend fun getCatalogProducts(): Result<List<Product>> {
        return try {
            val response = apiService.getCatalogProducts()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSellerProducts(sellerId: Int): Result<List<Product>> {
        return try {
            val response = apiService.getSellerProducts(sellerId)
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createProduct(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        imagenUrl: String?
    ): Result<Product> {
        return try {
            val response = apiService.createProduct(
                ProductCreateRequest(nombre, descripcion, precio, stock, imagenUrl)
            )
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(
        productId: Int,
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        imagenUrl: String?
    ): Result<Product> {
        return try {
            val response = apiService.updateProduct(
                productId,
                ProductCreateRequest(nombre, descripcion, precio, stock, imagenUrl)
            )
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: Int): Result<String> {
        return try {
            val response = apiService.deleteProduct(productId)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProductImage(): Result<String> {
        return try {
            val response = apiService.uploadProductImage()
            Result.success(response.imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
