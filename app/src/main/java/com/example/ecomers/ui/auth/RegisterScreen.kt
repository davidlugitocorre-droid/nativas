package com.example.ecomers.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecomers.ui.navigation.Screen
import com.example.ecomers.ui.theme.MainGradientEnd
import com.example.ecomers.ui.theme.MainGradientStart

/**
 * Pantalla de Registro de Cuentas: RegisterScreen
 * 
 * MÓDULO 1: Inicio de Sesión y Roles de Usuario.
 * Permite registrarse como Vendedor o Comprador de manera interactiva.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateFlowOf("") }
    var email by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var selectedRole by remember { mutableStateFlowOf("comprador") } // Rol por defecto
    var showPassword by remember { mutableStateFlowOf(false) }

    val registerState by authViewModel.registerState.collectAsState()

    // Manejar el éxito del registro
    LaunchedEffect(registerState) {
        if (registerState is AuthState.Success) {
            val user = (registerState as AuthState.Success).user
            Toast.makeText(context, "¡Registro Exitoso! Bienvenido ${user.fullName}", Toast.LENGTH_LONG).show()
            authViewModel.clearErrors()
            
            // Redirigir según el rol elegido
            val route = when (user.rol) {
                com.example.ecomers.domain.model.UserRole.ADMIN -> Screen.AdminDashboard.route
                com.example.ecomers.domain.model.UserRole.SELLER -> Screen.SellerDashboard.route
                com.example.ecomers.domain.model.UserRole.BUYER -> Screen.BuyerDashboard.route
            }
            navController.navigate(route) {
                popUpTo(Screen.Register.route) { inclusive = true }
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Encabezado Gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(MainGradientStart, MainGradientEnd)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Crear Cuenta",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Únete a la mejor experiencia Marketplace",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Formulario Estilo Card Elevado
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 150.dp, bottom = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input Nombre
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nombre Completo") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Correo
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Input Contraseña
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    trailingIcon = {
                        val image = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(image, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Selección Premium de Rol (Segmented Control / Selectable Cards)
                Text(
                    text = "Seleccione su Rol",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tarjeta de Opción: Comprador
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "comprador" },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRole == "comprador") MaterialTheme.colorScheme.primaryContainer 
                                             else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selectedRole == "comprador") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Person, 
                                contentDescription = null,
                                tint = if (selectedRole == "comprador") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Comprador", 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRole == "comprador") MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                            )
                        }
                    }

                    // Tarjeta de Opción: Vendedor
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedRole = "vendedor" },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedRole == "vendedor") MaterialTheme.colorScheme.primaryContainer 
                                             else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (selectedRole == "vendedor") BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Storefront, 
                                contentDescription = null,
                                tint = if (selectedRole == "vendedor") MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Vendedor", 
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedRole == "vendedor") MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Enviar Registro
                Button(
                    onClick = { authViewModel.register(email, fullName, password, selectedRole) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = registerState !is AuthState.Loading
                ) {
                    if (registerState is AuthState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Registrar Cuenta", color = Color.White)
                    }
                }

                // Mostrar error si existe
                if (registerState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (registerState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Enlace a Login
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("¿Ya tiene una cuenta? Iniciar Sesión", fontSize = 13.sp)
                }
            }
        }
    }
}
