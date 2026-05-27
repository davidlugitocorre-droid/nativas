package com.example.ecomers.di

import android.content.Context
import com.example.ecomers.data.local.pref.SecurityPreferences
import com.example.ecomers.data.remote.api.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Módulo de Inyección de Dependencias: NetworkModule
 * 
 * Configura las dependencias globales de red para la aplicación.
 * Proporciona el cliente HTTP OkHttp, interceptores de autenticación JWT,
 * el parseador Gson y el servicio Retrofit.
 * 
 * Se instala en el componente Singleton (toda la vida útil de la app).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://lugo-ecommerce-backend.onrender.com/" // URL del backend en producción (Render)

    @Provides
    @Singleton
    fun provideSecurityPreferences(@ApplicationContext context: Context): SecurityPreferences {
        return SecurityPreferences(context)
    }

    /**
     * Proporciona un Interceptor de Red para adjuntar automáticamente el Token JWT
     * en el encabezado de "Authorization: Bearer <token>" de cada consulta saliente.
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(securityPrefs: SecurityPreferences): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = securityPrefs.getToken()

            val requestBuilder = originalRequest.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            
            chain.proceed(requestBuilder.build())
        }
    }

    /**
     * Proporciona el cliente HTTP configurado con tiempos de espera optimizados
     * e interceptores para monitorear las llamadas HTTP por consola y gestionar seguridad JWT.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Proporciona la instancia centralizada de Retrofit con Gson y OkHttpClient.
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Proporciona la implementación concreta de los Endpoints de red.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
