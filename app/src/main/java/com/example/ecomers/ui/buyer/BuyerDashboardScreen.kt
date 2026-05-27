package com.example.ecomers.ui.buyer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecomers.domain.model.CartItem
import com.example.ecomers.domain.model.Order
import com.example.ecomers.domain.model.Product
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.navigation.Screen

/**
 * Panel del Comprador: BuyerDashboardScreen
 * 
 * MÓDULO 5: Exploración de productos, gestión reactiva de carrito persistido en Room
 * y consulta del historial de órdenes en PostgreSQL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerDashboardScreen(
    navController: NavController,
    buyerViewModel: BuyerViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(0) } // 0: Catálogo, 1: Carrito, 2: Historial

    val catalogState by buyerViewModel.catalogState.collectAsState()
    val cartItems by buyerViewModel.cartItems.collectAsState()
    val orderHistoryState by buyerViewModel.orderHistoryState.collectAsState()

    // Cargar historial de compras al entrar a la pestaña correspondiente
    LaunchedEffect(currentTab) {
        if (currentTab == 2) {
            buyerViewModel.loadOrderHistory()
        } else if (currentTab == 0) {
            buyerViewModel.loadCatalog()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            0 -> "Catálogo de Productos"
                            1 -> "Mi Carrito de Compras"
                            else -> "Mis Compras"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (currentTab == 0) buyerViewModel.loadCatalog()
                        else if (currentTab == 2) buyerViewModel.loadOrderHistory()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            buyerViewModel.clearCart() // Opcional: Limpiar carrito local al salir
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                // Pestaña Catálogo
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                    label = { Text("Catálogo") }
                )
                // Pestaña Carrito (Con badge reactivo de conteo)
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (cartItems.isNotEmpty()) {
                                    Badge { Text(cartItems.sumOf { it.cantidad }.toString()) }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        }
                    },
                    label = { Text("Carrito") }
                )
                // Pestaña Historial
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("Historial") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                0 -> CatalogTab(
                    catalogState = catalogState,
                    onAddToCart = { product ->
                        buyerViewModel.addToCart(product)
                        Toast.makeText(context, "${product.nombre} añadido al carrito", Toast.LENGTH_SHORT).show()
                    }
                )
                1 -> CartTab(
                    cartItems = cartItems,
                    onQtyChange = { product, newQty ->
                        buyerViewModel.updateCartQuantity(product, newQty)
                    },
                    onRemove = { item ->
                        buyerViewModel.removeFromCart(item)
                    },
                    onCheckout = {
                        navController.navigate(Screen.Checkout.route)
                    }
                )
                2 -> HistoryTab(
                    orderHistoryState = orderHistoryState,
                    onReload = { buyerViewModel.loadOrderHistory() }
                )
            }
        }
    }
}

// ==========================================
// 1. PESTAÑA: CATÁLOGO DE PRODUCTOS
// ==========================================
@Composable
fun CatalogTab(
    catalogState: CatalogState,
    onAddToCart: (Product) -> Unit
) {
    when (catalogState) {
        is CatalogState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CatalogState.Success -> {
            val list = catalogState.products
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay productos disponibles en este momento.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list) { product ->
                        BuyerProductCard(product = product, onAddClick = { onAddToCart(product) })
                    }
                }
            }
        }
        is CatalogState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = catalogState.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun BuyerProductCard(
    product: Product,
    onAddClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = product.imagenUrl ?: "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
                contentDescription = product.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.sellerName ?: "Vendedor",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${product.precio}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                    
                    if (product.stock > 0) {
                        IconButton(
                            onClick = onAddClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        }
                    } else {
                        Text("Agotado", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. PESTAÑA: CARRITO DE COMPRAS (ROOM REACTIVO)
// ==========================================
@Composable
fun CartTab(
    cartItems: List<CartItem>,
    onQtyChange: (Product, Int) -> Unit,
    onRemove: (CartItem) -> Unit,
    onCheckout: () -> Unit
) {
    if (cartItems.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("El carrito está vacío", color = Color.Gray)
            }
        }
        return
    }

    val total = cartItems.sumOf { it.product.precio * it.cantidad }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(cartItems) { item ->
                CartCard(item = item, onQtyChange = onQtyChange, onRemove = onRemove)
            }
        }
        
        // Fila Inferior con Total y Checkout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a pagar:", fontSize = 16.sp, color = Color.Gray)
                    Text("$${String.format("%.2f", total)}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceder al Pago", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CartCard(
    item: CartItem,
    onQtyChange: (Product, Int) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.imagenUrl ?: "https://images.unsplash.com/photo-1542291026-7eec264c27ff",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$${item.product.precio}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                
                // Controladores de cantidad (+/-)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onQtyChange(item.product, item.cantidad - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                    }
                    Text(item.cantidad.toString(), fontWeight = FontWeight.Bold)
                    IconButton(
                        onClick = { onQtyChange(item.product, item.cantidad + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                }
            }
            IconButton(onClick = { onRemove(item) }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ==========================================
// 3. PESTAÑA: HISTORIAL DE ÓRDENES
// ==========================================
@Composable
fun HistoryTab(
    orderHistoryState: OrderHistoryState,
    onReload: () -> Unit
) {
    when (orderHistoryState) {
        is OrderHistoryState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is OrderHistoryState.Success -> {
            val list = orderHistoryState.orders
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Aún no has realizado ninguna compra.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list) { order ->
                        OrderCard(order = order)
                    }
                }
            }
        }
        is OrderHistoryState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = orderHistoryState.message, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onReload) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Compra #${order.id}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                
                // Badge de Pago
                val statusText = order.estadoPago.value
                val isPaid = order.estadoPago == com.example.ecomers.domain.model.PaymentStatus.APROBADO
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isPaid) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                    )
                ) {
                    Text(
                        text = statusText.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPaid) Color(0xFF065F46) else Color(0xFF991B1B),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
            Text("Fecha: ${order.createdAt ?: "Reciente"}", fontSize = 12.sp, color = Color.Gray)
            Text("Valor Total: $${String.format("%.2f", order.total)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            if (order.idTransaccionEpayco != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Transacción: ${order.idTransaccionEpayco}", fontSize = 11.sp, color = Color.DarkGray)
            }
            if (order.latitud != null && order.longitud != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Map, contentDescription = null, size = 12.dp, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS: ${String.format("%.4f", order.latitud)}, ${String.format("%.4f", order.longitud)}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = Modifier.size(size), tint = tint)
}
