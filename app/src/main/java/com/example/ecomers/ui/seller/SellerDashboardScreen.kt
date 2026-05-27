package com.example.ecomers.ui.seller

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.ecomers.domain.model.Product
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.navigation.Screen

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
                title = { Text("Mi Tienda", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = { navController.navigate(Screen.SellerProductCrud.createRoute(-1)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Crear Producto")
            }
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
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aún no tienes productos en catálogo. ¡Crea el primero!")
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Coil AsyncImage con carga suave
            AsyncImage(
                model = product.imagenUrl ?: "https://images.unsplash.com/photo-1542291026-7eec264c27ff", // Fallback elegante
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
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$${product.precio}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Nivel de Stock
                Text(
                    text = "Stock: ${product.stock} uds",
                    fontSize = 11.sp,
                    color = if (product.stock == 0) MaterialTheme.colorScheme.error else Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
