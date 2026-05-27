package com.example.ecomers.ui.seller

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecomers.domain.model.Product
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.navigation.Screen
import com.example.ecomers.ui.theme.MainGradientEnd
import com.example.ecomers.ui.theme.MainGradientStart

/**
 * Consola de Inventario del Vendedor: SellerDashboardScreen
 * 
 * MÓDULO 4: Publicación y CRUD de Productos.
 * Permite visualizar el stock, editar campos y eliminar ítems.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    navController: NavController,
    sellerViewModel: SellerViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val productsState by sellerViewModel.productsState.collectAsState()
    val productCrudState by sellerViewModel.productCrudState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<Product?>(null) }

    LaunchedEffect(key1 = true) {
        sellerViewModel.loadSellerProducts()
    }

    // Escuchar el resultado de modificaciones
    LaunchedEffect(productCrudState) {
        if (productCrudState is ProductCrudState.Success) {
            Toast.makeText(context, (productCrudState as ProductCrudState.Success).message, Toast.LENGTH_SHORT).show()
            sellerViewModel.clearCrudState()
        } else if (productCrudState is ProductCrudState.Error) {
            Toast.makeText(context, (productCrudState as ProductCrudState.Error).message, Toast.LENGTH_LONG).show()
            sellerViewModel.clearCrudState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Mi Tienda", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "Panel de Vendedor",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { sellerViewModel.loadSellerProducts() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    IconButton(
                        onClick = {
                            authViewModel.logout()
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
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.SellerProductCrud.createRoute(-1)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Producto", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (productsState) {
                is SellerProductsState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SellerProductsState.Success -> {
                    val products = (productsState as SellerProductsState.Success).products
                    if (products.isEmpty()) {
                        // Estado vacío premium
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Storefront,
                                    contentDescription = null,
                                    modifier = Modifier.size(72.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Tu tienda está vacía",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "¡Crea tu primer producto y empieza a vender!",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Stats bar
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${products.size}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Productos", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${products.sumOf { it.stock }}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text("Stock Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            "${products.count { it.stock == 0 }}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 22.sp,
                                            color = if (products.any { it.stock == 0 }) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                        )
                                        Text("Agotados", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                }
                            }

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(products) { product ->
                                    SellerProductCard(
                                        product = product,
                                        onEdit = {
                                            navController.navigate(Screen.SellerProductCrud.createRoute(product.id))
                                        },
                                        onDelete = {
                                            showDeleteDialog = product
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                is SellerProductsState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (productsState as SellerProductsState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { sellerViewModel.loadSellerProducts() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmación de Borrado de Producto
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Producto") },
            text = { Text("¿Está seguro de que desea eliminar permanentemente a ${showDeleteDialog?.nombre} del catálogo? Se borrará de PostgreSQL.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { sellerViewModel.deleteProduct(it.id) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Componente Visual Premium: Tarjeta de Producto del Vendedor
 * Muestra la imagen del producto cargada desde Cloudinary con diseño moderno.
 */
@Composable
fun SellerProductCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Imagen del producto con overlay de stock
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = product.imagenUrl,
                    contentDescription = product.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                
                // Si no hay imagen, mostrar placeholder elegante
                if (product.imagenUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MainGradientStart.copy(alpha = 0.15f),
                                        MainGradientEnd.copy(alpha = 0.25f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    }
                }

                // Badge de stock en la esquina superior derecha
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (product.stock > 0)
                            Color(0xFF065F46).copy(alpha = 0.9f)
                        else
                            MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = if (product.stock > 0) "${product.stock} uds" else "Agotado",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$${String.format("%,.2f", product.precio)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Editar
                    FilledTonalButton(
                        onClick = onEdit,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Editar", fontSize = 11.sp)
                    }
                    // Botón Eliminar
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Borrar", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
