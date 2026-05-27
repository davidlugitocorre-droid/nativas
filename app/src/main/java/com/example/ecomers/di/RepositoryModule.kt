package com.example.ecomers.di

import com.example.ecomers.data.repository.AuthRepositoryImpl
import com.example.ecomers.data.repository.OrderRepositoryImpl
import com.example.ecomers.data.repository.ProductRepositoryImpl
import com.example.ecomers.data.repository.UserRepositoryImpl
import com.example.ecomers.domain.repository.AuthRepository
import com.example.ecomers.domain.repository.OrderRepository
import com.example.ecomers.domain.repository.ProductRepository
import com.example.ecomers.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo de Inyección de Dependencias: RepositoryModule
 * 
 * Vincula las interfaces de Dominio (contratos) con sus implementaciones
 * concretas en la capa de Datos (Data).
 * 
 * De esta forma, cualquier ViewModel o Caso de Uso que solicite una interfaz
 * recibirá automáticamente la implementación orquestada con Retrofit y Base de Datos.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository
}
