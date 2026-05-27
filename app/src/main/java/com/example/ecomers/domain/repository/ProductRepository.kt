package com.example.ecomers.domain.repository

import android.net.Uri
import com.example.ecomers.domain.model.Product

/**
 * Contrato de Dominio: ProductRepository
 * 
 * Define las operaciones requeridas por el Módulo 4 (CRUD de Productos del Vendedor)
 * y el Módulo 5 (Visualización del catálogo por parte del Comprador).
 */
interface ProductRepository {

    /**
     * Obtiene el listado completo de productos del catálogo global.
     */
    suspend fun getCatalogProducts(): Result<List<Product>>

    /**
     * Obtiene los productos creados por un vendedor específico.
     */
    suspend fun getSellerProducts(
        sellerId: Int
    ): Result<List<Product>>

    /**
     * Crea un nuevo producto asociándolo al vendedor autenticado.
     */
    suspend fun createProduct(
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        imagenUrl: String?
    ): Result<Product>

    /**
     * Edita los campos de un producto existente.
     */
    suspend fun updateProduct(
        productId: Int,
        nombre: String,
        descripcion: String?,
        precio: Double,
        stock: Int,
        imagenUrl: String?
    ): Result<Product>

    /**
     * Elimina permanentemente un producto de PostgreSQL.
     */
    suspend fun deleteProduct(
        productId: Int
    ): Result<String>

    /**
     * Sube una imagen a Cloudinary a través del backend.
     * Retorna la URL pública de la imagen almacenada en la nube.
     */
    suspend fun uploadProductImage(imageUri: Uri): Result<String>
}
