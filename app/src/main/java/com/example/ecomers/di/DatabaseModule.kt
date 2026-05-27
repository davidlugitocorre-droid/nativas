package com.example.ecomers.di

import android.content.Context
import androidx.room.Room
import com.example.ecomers.data.local.room.CartDao
import com.example.ecomers.data.local.room.CartDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Inyección de Dependencias: DatabaseModule
 * 
 * Proporciona y configura la base de datos Room de persistencia local en SQLite.
 * Se encarga de instanciar de forma única (Singleton) el acceso al Carrito de compras.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Instancia la base de datos Room CartDatabase local.
     */
    @Provides
    @Singleton
    fun provideCartDatabase(
        @ApplicationContext context: Context
    ): CartDatabase {
        return Room.databaseBuilder(
            context,
            CartDatabase::class.java,
            "ecomers_shopping_cart_db"
        )
        .fallbackToDestructiveMigration() // Manejo automático ante cambios de esquemas de datos locales
        .build()
    }

    /**
     * Proporciona la interfaz DAO para manipular de manera directa los items del carrito.
     */
    @Provides
    @Singleton
    fun provideCartDao(database: CartDatabase): CartDao {
        return database.cartDao()
    }
}
