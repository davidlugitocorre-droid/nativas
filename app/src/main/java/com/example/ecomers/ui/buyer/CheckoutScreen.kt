package com.example.ecomers.ui.buyer

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecomers.ui.navigation.Screen

/**
 * Pantalla de Checkout: CheckoutScreen
 * 
 * MÓDULO 6: Captura de Geolocalización Física (GPS Satelital).
 * MÓDULO 7: Simulación robusta de ePayco y limpieza de base de datos local Room.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    buyerViewModel: BuyerViewModel
) {
    val context = LocalContext.current
    val cartItems by buyerViewModel.cartItems.collectAsState()
    val latitude by buyerViewModel.latitude.collectAsState()
    val longitude by buyerViewModel.longitude.collectAsState()
    val checkoutState by buyerViewModel.checkoutState.collectAsState()

    val total = cartItems.sumOf { it.product.precio * it.cantidad }

    // Lanzador de Permisos en Tiempo de Ejecución (Módulo 6)
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocationGranted || coarseLocationGranted) {
            buyerViewModel.fetchUserLocation()
        } else {
            Toast.makeText(context, "El permiso de ubicación es obligatorio para realizar el despacho físico.", Toast.LENGTH_LONG).show()
        }
    }

    // Escuchar el estado de finalización del pago
    LaunchedEffect(checkoutState) {
        if (checkoutState is CheckoutState.Success) {
            Toast.makeText(context, (checkoutState as CheckoutState.Success).message, Toast.LENGTH_LONG).show()
            buyerViewModel.clearCheckoutState()
            
            // Redirigir al historial para ver la orden aprobada
            navController.navigate(Screen.BuyerDashboard.route) {
                popUpTo(Screen.Checkout.route) { inclusive = true }
            }
        } else if (checkoutState is CheckoutState.Error) {
            Toast.makeText(context, (checkoutState as CheckoutState.Error).message, Toast.LENGTH_LONG).show()
            buyerViewModel.clearCheckoutState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finalizar Compra", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Resumen Financiero
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Resumen del Carrito", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Productos agregados:", fontSize = 15.sp)
                            Text("${cartItems.sumOf { it.cantidad }} unidades", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total a Facturar:", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%.2f", total)}", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // MÓDULO 6: Panel de Geolocalización Requerida
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (latitude != null) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (latitude != null) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (latitude != null) Icons.Default.LocationOn else Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (latitude != null) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (latitude != null) "Ubicación GPS Capturada" else "Geolocalización Requerida",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (latitude != null) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        }

                        if (latitude != null && longitude != null) {
                            Text(
                                text = "Latitud: ${String.format("%.6f", latitude)}\nLongitud: ${String.format("%.6f", longitude)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                        } else {
                            Text(
                                text = "Para procesar el checkout, es obligatorio capturar sus coordenadas GPS físicas actuales para despachar su compra.",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }

                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (latitude != null) Color(0xFF10B981) else MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = checkoutState !is CheckoutState.Locating
                        ) {
                            if (checkoutState is CheckoutState.Locating) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                            } else {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (latitude != null) "Recapturar Ubicación" else "Capturar Coordenadas GPS", color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // MÓDULO 7: Botón para realizar Pago con ePayco
                Button(
                    onClick = {
                        if (latitude == null || longitude == null) {
                            Toast.makeText(context, "Por favor capte su ubicación GPS primero.", Toast.LENGTH_LONG).show()
                        } else {
                            buyerViewModel.performCheckout(total)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = checkoutState !is CheckoutState.Loading && latitude != null
                ) {
                    if (checkoutState is CheckoutState.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pagar de forma Segura con ePayco", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
