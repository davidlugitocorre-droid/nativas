package com.example.ecomers.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ecomers.ui.admin.AdminDashboardScreen
import com.example.ecomers.ui.admin.AdminUserCrudScreen
import com.example.ecomers.ui.admin.AdminViewModel
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.auth.ForgotPasswordScreen
import com.example.ecomers.ui.auth.LoginScreen
import com.example.ecomers.ui.auth.RegisterScreen
import com.example.ecomers.ui.buyer.BuyerDashboardScreen
import com.example.ecomers.ui.buyer.BuyerViewModel
import com.example.ecomers.ui.buyer.CheckoutScreen
import com.example.ecomers.ui.seller.SellerDashboardScreen
import com.example.ecomers.ui.seller.SellerProductCrudScreen
import com.example.ecomers.ui.seller.SellerViewModel
import com.example.ecomers.ui.splash.SplashScreen

/**
 * Enrutamiento Principal de la App: AppNavigation
 * 
 * Configura la navegación declarativa multi-pantalla y multi-rol para Android.
 * Inyecta automáticamente los ViewModels de Hilt mediante [hiltViewModel()] garantizando
 * el desacoplamiento de dependencias y la retención del estado ante rotaciones de pantalla.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    // Instanciar el ViewModel de Autenticación para compartirlo en flujos públicos si se requiere
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        
        // ==========================================
        // 1. PANTALLAS PÚBLICAS Y SEGURIDAD (Módulos 1 y 2)
        // ==========================================
        
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Login.route) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController, authViewModel = authViewModel)
        }

        // ==========================================
        // 2. MÓDULO 3: VISTA ADMINISTRADOR (CRUD Usuarios PostgreSQL)
        // ==========================================
        
        composable(Screen.AdminDashboard.route) {
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminDashboardScreen(
                navController = navController, 
                adminViewModel = adminViewModel, 
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.AdminUserCrud.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: -1
            val adminViewModel: AdminViewModel = hiltViewModel()
            AdminUserCrudScreen(
                navController = navController,
                adminViewModel = adminViewModel,
                userId = userId
            )
        }

        // ==========================================
        // 3. MÓDULO 4: VISTA VENDEDOR (Inventario e Imágenes)
        // ==========================================
        
        composable(Screen.SellerDashboard.route) {
            val sellerViewModel: SellerViewModel = hiltViewModel()
            SellerDashboardScreen(
                navController = navController,
                sellerViewModel = sellerViewModel,
                authViewModel = authViewModel
            )
        }

        composable(
            route = Screen.SellerProductCrud.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: -1
            val sellerViewModel: SellerViewModel = hiltViewModel()
            SellerProductCrudScreen(
                navController = navController,
                sellerViewModel = sellerViewModel,
                productId = productId
            )
        }

        // ==========================================
        // 4. MÓDULOS 5, 6 y 7: VISTA COMPRADOR (Exploración, Carrito, GPS, ePayco)
        // ==========================================
        
        composable(Screen.BuyerDashboard.route) {
            val buyerViewModel: BuyerViewModel = hiltViewModel()
            BuyerDashboardScreen(
                navController = navController,
                buyerViewModel = buyerViewModel,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Checkout.route) {
            val buyerViewModel: BuyerViewModel = hiltViewModel()
            CheckoutScreen(
                navController = navController,
                buyerViewModel = buyerViewModel
            )
        }
    }
}
