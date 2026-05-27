package com.example.ecomers

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Clase de Aplicación Global: EcomersApp
 * 
 * Es el punto de inicio de la aplicación.
 * La anotación [@HiltAndroidApp] es OBLIGATORIA para iniciar la generación de código
 * de Dagger Hilt en tiempo de compilación. Inicializa todos los contenedores de dependencias
 * que serán inyectados a lo largo del proyecto (capas de datos, red, biometría, etc).
 */
@HiltAndroidApp
class EcomersApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializaciones globales que requiera el sistema pueden ir aquí
    }
}
