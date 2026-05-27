package com.example.ecomers.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Base de Datos Local: CartDatabase
 * 
 * Clase abstracta que define las bases de datos de SQLite en Android gestionadas por Room.
 * Especifica la lista de entidades persistidas y la versión del esquema.
 */
@Database(entities = [CartEntity::class], version = 1, exportSchema = false)
abstract class CartDatabase : RoomDatabase() {
    
    /**
     * Proporciona acceso al Data Access Object (DAO) para realizar operaciones del carrito.
     */
    abstract fun cartDao(): CartDao
}
