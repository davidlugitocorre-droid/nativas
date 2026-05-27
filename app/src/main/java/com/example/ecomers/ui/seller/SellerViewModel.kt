package com.example.ecomers.ui.seller

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomers.domain.model.Product
import com.example.ecomers.domain.repository.AuthRepository
import com.example.ecomers.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del Vendedor: SellerViewModel
 * 
 * Orquesta y administra los estados de la UI del Módulo 4 (Gestión de Productos e Imágenes).
 * Consume los endpoints correspondientes al rol de Vendedor.
 */
@HiltViewModel
class SellerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- ESTADOS DE LA UI ---
    
    // Lista de productos creados por este vendedor
    private val _productsState = MutableStateFlow<SellerProductsState>(SellerProductsState.Loading)
    val productsState: StateFlow<SellerProductsState> = _productsState.asStateFlow()

    // Estado del formulario de creación/edición
    private val _productCrudState = MutableStateFlow<ProductCrudState>(ProductCrudState.Idle)
    val productCrudState: StateFlow<ProductCrudState> = _productCrudState.asStateFlow()

    // Detalle de un producto individual para rellenar formulario de edición
    private val _selectedProductState = MutableStateFlow<ProductDetailState>(ProductDetailState.Idle)
    val selectedProductState: StateFlow<ProductDetailState> = _selectedProductState.asStateFlow()

    // Estado del proceso de subida de imágenes (Cámara/Galería)
    private val _imageUploadState = MutableStateFlow<ImageState>(ImageState.Idle)
    val imageUploadState: StateFlow<ImageState> = _imageUploadState.asStateFlow()

    init {
        loadSellerProducts()
    }

    /**
     * Carga únicamente los productos asociados al vendedor actualmente logueado.
     */
    fun loadSellerProducts() {
        val seller = authRepository.getLoggedUser()
        if (seller == null) {
            _productsState.value = SellerProductsState.Error("No se encontró sesión del vendedor activa")
            return
        }

        _productsState.value = SellerProductsState.Loading
        viewModelScope.launch {
            productRepository.getSellerProducts(seller.id)
                .onSuccess { list ->
                    _productsState.value = SellerProductsState.Success(list)
                }
                .onFailure { error ->
                    _productsState.value = SellerProductsState.Error(
                        error.localizedMessage ?: "Error al cargar el inventario del vendedor"
                    )
                }
        }
    }

    /**
     * Carga el detalle de un producto específico para rellenar campos en la UI de edición.
     */
    fun loadProductDetails(productId: Int) {
        if (productId == -1) {
            _selectedProductState.value = ProductDetailState.Idle
            return
        }

        _selectedProductState.value = ProductDetailState.Loading
        viewModelScope.launch {
            // Buscamos en la lista local cargada por rendimiento, o cargamos de red
            val currentListState = _productsState.value
            if (currentListState is SellerProductsState.Success) {
                val matched = currentListState.products.find { it.id == productId }
                if (matched != null) {
                    _selectedProductState.value = ProductDetailState.Success(matched)
                    return@launch
                }
            }
            
            // Si no se encuentra en caché local, podemos volver a consultar el catálogo
            productRepository.getCatalogProducts()
                .onSuccess { list ->
                    val matched = list.find { it.id == productId }
                    if (matched != null) {
                        _selectedProductState.value = ProductDetailState.Success(matched)
                    } else {
                        _selectedProductState.value = ProductDetailState.Error("Producto no encontrado")
                    }
                }
                .onFailure { error ->
                    _selectedProductState.value = ProductDetailState.Error(error.localizedMessage ?: "Error al buscar producto")
                }
        }
    }

    /**
     * Crea un nuevo producto y lo asocia automáticamente al vendedor de la sesión.
     */
    fun createProduct(nombre: String, descripcion: String?, precio: Double, stock: Int, imagenUrl: String?) {
        if (nombre.isBlank() || precio <= 0.0 || stock < 0) {
            _productCrudState.value = ProductCrudState.Error("Verifique los campos ingresados")
            return
        }

        _productCrudState.value = ProductCrudState.Loading
        viewModelScope.launch {
            productRepository.createProduct(nombre, descripcion, precio, stock, imagenUrl)
                .onSuccess {
                    _productCrudState.value = ProductCrudState.Success("Producto agregado al catálogo correctamente")
                    loadSellerProducts()
                }
                .onFailure { error ->
                    _productCrudState.value = ProductCrudState.Error(error.localizedMessage ?: "Error al guardar producto")
                }
        }
    }

    /**
     * Actualiza los datos de un producto del vendedor.
     */
    fun updateProduct(productId: Int, nombre: String, descripcion: String?, precio: Double, stock: Int, imagenUrl: String?) {
        if (nombre.isBlank() || precio <= 0.0 || stock < 0) {
            _productCrudState.value = ProductCrudState.Error("Verifique los campos del formulario")
            return
        }

        _productCrudState.value = ProductCrudState.Loading
        viewModelScope.launch {
            productRepository.updateProduct(productId, nombre, descripcion, precio, stock, imagenUrl)
                .onSuccess {
                    _productCrudState.value = ProductCrudState.Success("Producto modificado correctamente")
                    loadSellerProducts()
                }
                .onFailure { error ->
                    _productCrudState.value = ProductCrudState.Error(error.localizedMessage ?: "Error al editar producto")
                }
        }
    }

    /**
     * Elimina el producto del vendedor.
     */
    fun deleteProduct(productId: Int) {
        _productCrudState.value = ProductCrudState.Loading
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
                .onSuccess { msg ->
                    _productCrudState.value = ProductCrudState.Success(msg)
                    loadSellerProducts()
                }
                .onFailure { error ->
                    _productCrudState.value = ProductCrudState.Error(error.localizedMessage ?: "Error al borrar producto")
                }
        }
    }

    /**
     * Simula la subida de una foto capturada por la Cámara o Galería al servidor de imágenes.
     * Retorna una URL pública para vincular al producto.
     */
    fun uploadProductImage() {
        _imageUploadState.value = ImageState.Uploading
        viewModelScope.launch {
            productRepository.uploadProductImage()
                .onSuccess { url ->
                    _imageUploadState.value = ImageState.Success(url)
                }
                .onFailure { error ->
                    _imageUploadState.value = ImageState.Error(error.localizedMessage ?: "Fallo al subir imagen")
                }
        }
    }

    fun clearCrudState() {
        _productCrudState.value = ProductCrudState.Idle
    }

    fun clearImageState() {
        _imageUploadState.value = ImageState.Idle
    }
}

// Estados del Inventario del Vendedor
sealed interface SellerProductsState {
    object Loading : SellerProductsState
    data class Success(val products: List<Product>) : SellerProductsState
    data class Error(val message: String) : SellerProductsState
}

// Estados de la Operación CRUD
sealed interface ProductCrudState {
    object Idle : ProductCrudState
    object Loading : ProductCrudState
    data class Success(val message: String) : ProductCrudState
    data class Error(val message: String) : ProductCrudState
}

// Estados de Carga de Detalle del Producto
sealed interface ProductDetailState {
    object Idle : ProductDetailState
    object Loading : ProductDetailState
    data class Success(val product: Product) : ProductDetailState
    data class Error(val message: String) : ProductDetailState
}

// Estados de Subida de Imagen
sealed interface ImageState {
    object Idle : ImageState
    object Uploading : ImageState
    data class Success(val imageUrl: String) : ImageState
    data class Error(val message: String) : ImageState
}
