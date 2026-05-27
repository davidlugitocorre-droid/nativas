package com.example.ecomers.ui.seller

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecomers.ui.auth.mutableStateFlowOf

/**
 * Formulario CRUD de Productos: SellerProductCrudScreen
 * 
 * MÓDULO 4: Permite al vendedor registrar un producto nuevo o editar uno existente,
 * con subida real de imágenes a Cloudinary desde la galería del dispositivo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProductCrudScreen(
    navController: NavController,
    sellerViewModel: SellerViewModel,
    productId: Int
) {
    val context = LocalContext.current
    val isEditMode = productId != -1

    var nombre by remember { mutableStateFlowOf("") }
    var descripcion by remember { mutableStateFlowOf("") }
    var precioText by remember { mutableStateFlowOf("") }
    var stockText by remember { mutableStateFlowOf("") }
    var imagenUrl by remember { mutableStateFlowOf<String?>(null) }

    // URI local de la imagen seleccionada (para preview inmediato antes de subir)
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val selectedProductState by sellerViewModel.selectedProductState.collectAsState()
    val productCrudState by sellerViewModel.productCrudState.collectAsState()
    val imageUploadState by sellerViewModel.imageUploadState.collectAsState()

    // Lanzador de la galería del dispositivo para seleccionar imágenes
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Subir automáticamente a Cloudinary al seleccionar
            sellerViewModel.uploadProductImage(it)
        }
    }

    // Cargar detalles de producto si estamos en modo edición
    LaunchedEffect(productId) {
        if (isEditMode) {
            sellerViewModel.loadProductDetails(productId)
        } else {
            nombre = ""
            descripcion = ""
            precioText = ""
            stockText = ""
            imagenUrl = null
            selectedImageUri = null
            sellerViewModel.clearImageState()
        }
    }

    // Escuchar cambios al cargar detalles del producto
    LaunchedEffect(selectedProductState) {
        if (isEditMode && selectedProductState is ProductDetailState.Success) {
            val product = (selectedProductState as ProductDetailState.Success).product
            nombre = product.nombre
            descripcion = product.descripcion ?: ""
            precioText = product.precio.toString()
            stockText = product.stock.toString()
            imagenUrl = product.imagenUrl
        }
    }

    // Escuchar el resultado de la subida de la imagen a Cloudinary
    LaunchedEffect(imageUploadState) {
        if (imageUploadState is ImageState.Success) {
            val url = (imageUploadState as ImageState.Success).imageUrl
            imagenUrl = url
            Toast.makeText(context, "¡Imagen subida a Cloudinary!", Toast.LENGTH_SHORT).show()
            sellerViewModel.clearImageState()
        } else if (imageUploadState is ImageState.Error) {
            Toast.makeText(context, (imageUploadState as ImageState.Error).message, Toast.LENGTH_LONG).show()
            sellerViewModel.clearImageState()
        }
    }

    // Escuchar la respuesta final de Guardar
    LaunchedEffect(productCrudState) {
        if (productCrudState is ProductCrudState.Success) {
            Toast.makeText(context, (productCrudState as ProductCrudState.Success).message, Toast.LENGTH_SHORT).show()
            sellerViewModel.clearCrudState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Producto" else "Agregar Producto") },
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
            if (isEditMode && selectedProductState is ProductDetailState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // --- SECTOR DE IMAGEN PREMIUM CON CLOUDINARY ---
                    Text(
                        "Imagen del Producto",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        // Mostrar imagen: preferir el URI local (preview rápido), luego la URL de Cloudinary
                        val displayModel: Any? = selectedImageUri ?: imagenUrl

                        if (displayModel != null) {
                            AsyncImage(
                                model = displayModel,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Overlay semitransparente con botón de cámara
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Cambiar Foto",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (imageUploadState is ImageState.Uploading) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Subiendo a Cloudinary...",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudUpload,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Tocar para seleccionar imagen",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    // Indicador de subida si hay imagen seleccionada pero aún está subiendo
                    if (imageUploadState is ImageState.Uploading && selectedImageUri != null) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Subiendo imagen a Cloudinary...",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // --- ENTRADAS DE FORMULARIO ---
                    
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Producto") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Precio
                        OutlinedTextField(
                            value = precioText,
                            onValueChange = { precioText = it },
                            label = { Text("Precio ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Stock
                        OutlinedTextField(
                            value = stockText,
                            onValueChange = { stockText = it },
                            label = { Text("Stock") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón Guardar
                    Button(
                        onClick = {
                            val precio = precioText.toDoubleOrNull() ?: 0.0
                            val stock = stockText.toIntOrNull() ?: 0
                            
                            if (isEditMode) {
                                sellerViewModel.updateProduct(productId, nombre, descripcion, precio, stock, imagenUrl)
                            } else {
                                sellerViewModel.createProduct(nombre, descripcion, precio, stock, imagenUrl)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = productCrudState !is ProductCrudState.Loading && imageUploadState !is ImageState.Uploading
                    ) {
                        if (productCrudState is ProductCrudState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Text("Guardar Cambios", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
