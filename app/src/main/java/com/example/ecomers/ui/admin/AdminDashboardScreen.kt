package com.example.ecomers.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
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
import com.example.ecomers.domain.model.User
import com.example.ecomers.ui.auth.AuthViewModel
import com.example.ecomers.ui.navigation.Screen
import com.example.ecomers.ui.theme.MainGradientEnd
import com.example.ecomers.ui.theme.MainGradientStart

/**
 * Panel de Administración: AdminDashboardScreen
 * 
 * MÓDULO 3: CRUD de Usuarios de la Base de Datos.
 * Renderiza el listado global de usuarios almacenados en PostgreSQL.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val usersState by adminViewModel.usersState.collectAsState()
    val crudState by adminViewModel.crudState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf<User?>(null) }

    // Disparar recarga inicial
    LaunchedEffect(key1 = true) {
        adminViewModel.loadUsers()
    }

    // Escuchar cambios en la escritura CRUD
    LaunchedEffect(crudState) {
        if (crudState is CrudState.Success) {
            Toast.makeText(context, (crudState as CrudState.Success).message, Toast.LENGTH_SHORT).show()
            adminViewModel.clearCrudState()
        } else if (crudState is CrudState.Error) {
            Toast.makeText(context, (crudState as CrudState.Error).message, Toast.LENGTH_LONG).show()
            adminViewModel.clearCrudState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Consola Admin",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Botón Refrescar
                    IconButton(onClick = { adminViewModel.loadUsers() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                    // Botón Cerrar Sesión
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.ExitToApp, 
                            contentDescription = "Cerrar Sesión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            // FAB para crear un nuevo usuario
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AdminUserCrud.createRoute(-1)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (usersState) {
                is UsersListState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UsersListState.Success -> {
                    val users = (usersState as UsersListState.Success).users
                    if (users.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No hay usuarios registrados en el sistema.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(users) { user ->
                                UserCard(
                                    user = user,
                                    onEdit = {
                                        navController.navigate(Screen.AdminUserCrud.createRoute(user.id))
                                    },
                                    onDelete = {
                                        showDeleteDialog = user
                                    }
                                )
                            }
                        }
                    }
                }
                is UsersListState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (usersState as UsersListState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { adminViewModel.loadUsers() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Confirmación de Borrado
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Está seguro de que desea eliminar a ${showDeleteDialog?.fullName}? Esta acción no se puede deshacer en PostgreSQL.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog?.let { adminViewModel.deleteUser(it.id) }
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
 * Componente Visual Premium: Tarjeta de Usuario
 */
@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icono decorativo de perfil
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Badge estilizado del Rol
                    Box(
                        modifier = Modifier
                            .background(
                                color = when (user.rol) {
                                    com.example.ecomers.domain.model.UserRole.ADMIN -> MaterialTheme.colorScheme.errorContainer
                                    com.example.ecomers.domain.model.UserRole.SELLER -> MaterialTheme.colorScheme.secondaryContainer
                                    com.example.ecomers.domain.model.UserRole.BUYER -> MaterialTheme.colorScheme.primaryContainer
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = user.rol.value.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (user.rol) {
                                com.example.ecomers.domain.model.UserRole.ADMIN -> MaterialTheme.colorScheme.onErrorContainer
                                com.example.ecomers.domain.model.UserRole.SELLER -> MaterialTheme.colorScheme.onSecondaryContainer
                                com.example.ecomers.domain.model.UserRole.BUYER -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            }

            // Acciones de Modificación
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
