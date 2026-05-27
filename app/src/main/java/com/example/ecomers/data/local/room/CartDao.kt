package com.example.ecomers.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Acceso a Datos Local: CartDao
 * 
 * Contiene las sentencias SQL y operaciones SQLite mapeadas por Room para el Carrito.
 * Retorna flujos reactivos (`Flow`) de Kotlin para actualizar la UI en tiempo real.
 */
@Dao
interface CartDao {

    /**
     * Obtiene el listado completo de productos agregados al carrito.
     * Retorna un Flow reactivo para observar cambios.
     */
    @Query("SELECT * FROM carrito_compras ORDER BY id ASC")
    fun getCartItems(): Flow<List<CartEntity>>

    /**
     * Inserta un producto en el carrito. Si ya existe, lo reemplaza (actualiza la cantidad).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartEntity)

    /**
     * Elimina un producto específico del carrito.
     */
    @Delete
    suspend fun deleteItem(item: CartEntity)

    /**
     * Limpia la tabla por completo al finalizar la compra (checkout exitoso).
     */
    @Query("DELETE FROM carrito_compras")
    suspend fun clearCart()
}
