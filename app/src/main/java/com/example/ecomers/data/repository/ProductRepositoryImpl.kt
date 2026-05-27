package com.example.ecomers.data.repository

import android.content.Context
import android.net.Uri
import com.example.ecomers.data.remote.api.ApiService
import com.example.ecomers.data.remote.model.ProductCreateRequest
import com.example.ecomers.domain.model.Product
import com.example.ecomers.domain.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación de Capa de Datos: ProductRepositoryImpl
 * 
 * Orquesta las peticiones REST API de productos interactuando con PostgreSQL
 * y subiendo imágenes reales a Cloudinary a través del backend.
 */
@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
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

    /**
     * Sube una imagen seleccionada por el usuario a Cloudinary mediante el backend.
     * Convierte el Uri del ContentResolver a un MultipartBody.Part para Retrofit.
     */
    override suspend fun uploadProductImage(imageUri: Uri): Result<String> {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: return Result.failure(Exception("No se pudo leer la imagen seleccionada"))
            
            val bytes = inputStream.readBytes()
            inputStream.close()
            
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", "product_image.jpg", requestBody)
            
            val response = apiService.uploadProductImage(part)
            Result.success(response.imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
