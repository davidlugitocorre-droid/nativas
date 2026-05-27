package com.example.ecomers

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.ecomers.ui.navigation.AppNavigation
import com.example.ecomers.ui.theme.LugoEcomersTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Actividad Principal: MainActivity
 * 
 * Es el punto de anclaje de nuestra UI declarativa en Compose.
 * La anotación [@AndroidEntryPoint] habilita a Hilt para inyectar dependencias y ViewModels
 * dentro de esta actividad y todas las pantallas e interfaces hijas que componen el flujo.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Aplicar la paleta de colores y estilos Premium del tema de Lugo E-commerce
            LugoEcomersTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    // Invocar la navegación modular multi-rol de la aplicación
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}
