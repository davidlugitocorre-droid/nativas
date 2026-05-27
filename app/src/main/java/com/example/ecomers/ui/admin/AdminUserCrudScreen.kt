package com.example.ecomers.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecomers.ui.auth.mutableStateFlowOf

/**
 * Formulario CRUD del Administrador: AdminUserCrudScreen
 * 
 * MÓDULO 3: Permite crear o editar usuarios con asignación directa de rol (Admin/Vendedor/Comprador).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserCrudScreen(
    navController: NavController,
    adminViewModel: AdminViewModel,
    userId: Int
) {
    val context = LocalContext.current
    val isEditMode = userId != -1

    var fullName by remember { mutableStateFlowOf("") }
    var email by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var role by remember { mutableStateFlowOf("comprador") } // "admin", "vendedor", "comprador"

    val selectedUserState by adminViewModel.selectedUserState.collectAsState()
    val crudState by adminViewModel.crudState.collectAsState()

    // Cargar detalles si es modo edición
    LaunchedEffect(userId) {
        if (isEditMode) {
            adminViewModel.loadUserDetails(userId)
        } else {
            // Limpiar datos
            fullName = ""
            email = ""
            password = ""
            role = "comprador"
        }
    }

    // Escuchar el estado de carga del usuario a editar
    LaunchedEffect(selectedUserState) {
        if (isEditMode && selectedUserState is UserDetailState.Success) {
            val user = (selectedUserState as UserDetailState.Success).user
            fullName = user.fullName
            email = user.email
            role = user.rol.value
        }
    }

    // Escuchar resultado de la transacción
    LaunchedEffect(crudState) {
        if (crudState is CrudState.Success) {
            Toast.makeText(context, (crudState as CrudState.Success).message, Toast.LENGTH_SHORT).show()
            adminViewModel.clearCrudState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Usuario" else "Crear Usuario") },
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
            if (isEditMode && selectedUserState is UserDetailState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Contraseña (Solo en creación)
                    if (!isEditMode) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Rol Dropdown o Segmented
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Rol de Sistema", 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("comprador", "vendedor", "admin").forEach { rolOption ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { role = rolOption },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (role == rolOption) MaterialTheme.colorScheme.primaryContainer 
                                                         else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = rolOption.uppercase(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (role == rolOption) MaterialTheme.colorScheme.onPrimaryContainer else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón Guardar
                    Button(
                        onClick = {
                            if (isEditMode) {
                                adminViewModel.updateUser(userId, email, fullName, role)
                            } else {
                                adminViewModel.createUser(email, fullName, password, role)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = crudState !is CrudState.Loading
                    ) {
                        if (crudState is CrudState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Guardar Cambios en Servidor", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
