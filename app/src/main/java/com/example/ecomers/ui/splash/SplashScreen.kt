package com.example.ecomers.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.auth.InitialTarget
import com.example.ecomers.ui.navigation.Screen
import com.example.ecomers.ui.theme.MainGradientEnd
import com.example.ecomers.ui.theme.MainGradientStart
import kotlinx.coroutines.delay

/**
 * Pantalla de Carga Inicial: SplashScreen
 * 
 * MÓDULO 1: Inicio de Sesión y Roles de Usuario.
 * Presenta un diseño premium, una transición suave de opacidad (fade-in),
 * e identifica la sesión encriptada activa para redirigir directamente al panel correspondiente.
 */
@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    // Animación de opacidad del logotipo
    val alphaAnim = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Disparar animación de desvanecimiento
        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200)
        )
        // Espera mínima estética de 1.8 segundos
        delay(1800)

        // Determinar destino en función de la presencia del Token JWT y rol
        val target = authViewModel.getInitialNavigationTarget()
        
        navController.navigate(
            when (target) {
                InitialTarget.LOGIN -> Screen.Login.route
                InitialTarget.ADMIN_DASHBOARD -> Screen.AdminDashboard.route
                InitialTarget.SELLER_DASHBOARD -> Screen.SellerDashboard.route
                InitialTarget.BUYER_DASHBOARD -> Screen.BuyerDashboard.route
            }
        ) {
            // Eliminar el Splash del Backstack para evitar que el usuario vuelva a él con el botón atrás
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Contenedor Visual con Gradiente Premium
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MainGradientStart, MainGradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            // Icono Minimalista de Carrito de E-commerce
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Logo Lugo Ecomers",
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Nombre de la Marca
            Text(
                text = "Lugo Ecomers",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu Marketplace Multi-Rol Seguro",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
