package com.example.ecomers.ui.navigation

/**
 * Enrutamiento de la Aplicación: Screen
 * 
 * Clase sellada (Sealed Class) que define de manera tipada y segura las rutas de
 * navegación utilizadas por Compose Navigation Component.
 * 
 * Contiene rutas con paso de parámetros para los CRUDs de Usuarios (Admin)
 * y de Productos (Vendedores).
 */
sealed class Screen(val route: String) {

    // --- Módulo 1 & 2: Flujos Públicos ---
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // --- Módulo 3: Flujo de Administrador ---
    object AdminDashboard : Screen("admin_dashboard")
    
    /**
     * Pantalla de Formulario de Creación/Edición de Usuarios.
     * Recibe [userId]. Si [userId] es -1, se interpreta como flujo de creación.
     */
    object AdminUserCrud : Screen("admin_user_crud/{userId}") {
        fun createRoute(userId: Int): String = "admin_user_crud/$userId"
    }

    // --- Módulo 4: Flujo de Vendedor ---
    object SellerDashboard : Screen("seller_dashboard")
    
    /**
     * Pantalla de Formulario de Creación/Edición de Productos.
     * Recibe [productId]. Si [productId] es -1, se interpreta como flujo de creación.
     */
    object SellerProductCrud : Screen("seller_product_crud/{productId}") {
        fun createRoute(productId: Int): String = "seller_product_crud/$productId"
    }

    // --- Módulo 5, 6 & 7: Flujo de Comprador ---
    object BuyerDashboard : Screen("buyer_dashboard")
    object Checkout : Screen("checkout")
    object OrderHistory : Screen("order_history")
}
