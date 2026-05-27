package com.example.ecomers.ui.buyer

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomers.data.local.room.CartDao
import com.example.ecomers.data.local.room.CartEntity
import com.example.ecomers.domain.model.CartItem
import com.example.ecomers.domain.model.Order
import com.example.ecomers.domain.model.Product
import com.example.ecomers.domain.repository.AuthRepository
import com.example.ecomers.domain.repository.OrderRepository
import com.example.ecomers.domain.repository.ProductRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del Comprador: BuyerViewModel
 * 
 * Orqueta las acciones para la exploración del catálogo (Módulo 5), la reactividad del
 * carrito de compras persistido en Room (Módulo 5), la geolocalización física para envíos
 * por Google Location (Módulo 6), y el checkout con la pasarela ePayco (Módulo 7).
 */
@HiltViewModel
class BuyerViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository,
    private val cartDao: CartDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // --- 1. ESTADO DEL CATÁLOGO ---
    private val _catalogState = MutableStateFlow<CatalogState>(CatalogState.Loading)
    val catalogState: StateFlow<CatalogState> = _catalogState.asStateFlow()

    // --- 2. ESTADO DEL CARRITO REACTIVO (ROOM) ---
    // Escucha de manera reactiva la tabla de SQLite y la mapea a objetos de dominio CartItem
    val cartItems: StateFlow<List<CartItem>> = cartDao.getCartItems()
        .map { list -> list.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- 3. ESTADOS DE GEOLOCALIZACIÓN Y CHECKOUT ---
    private val _latitude = MutableStateFlow<Double?>(null)
    val latitude: StateFlow<Double?> = _latitude.asStateFlow()

    private val _longitude = MutableStateFlow<Double?>(null)
    val longitude: StateFlow<Double?> = _longitude.asStateFlow()

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState.asStateFlow()

    // --- 4. HISTORIAL DE ÓRDENES ---
    private val _orderHistoryState = MutableStateFlow<OrderHistoryState>(OrderHistoryState.Loading)
    val orderHistoryState: StateFlow<OrderHistoryState> = _orderHistoryState.asStateFlow()

    init {
        loadCatalog()
        loadOrderHistory()
    }

    /**
     * Obtiene el listado completo de productos habilitados para compra.
     */
    fun loadCatalog() {
        _catalogState.value = CatalogState.Loading
        viewModelScope.launch {
            productRepository.getCatalogProducts()
                .onSuccess { list ->
                    _catalogState.value = CatalogState.Success(list)
                }
                .onFailure { error ->
                    _catalogState.value = CatalogState.Error(
                        error.localizedMessage ?: "Error de red al consultar el catálogo"
                    )
                }
        }
    }

    /**
     * Mapea y guarda un producto seleccionado en la base de datos de Room.
     */
    fun addToCart(product: Product, quantity: Int = 1) {
        viewModelScope.launch {
            // Buscamos si ya está en la lista para incrementar
            val existing = cartItems.value.find { it.product.id == product.id }
            val newQty = (existing?.cantidad ?: 0) + quantity
            
            // Limitamos al stock del producto
            val finalQty = if (newQty > product.stock) product.stock else newQty
            
            if (finalQty > 0) {
                cartDao.insertOrUpdate(
                    CartEntity(
                        id = product.id,
                        sellerId = product.sellerId,
                        nombre = product.nombre,
                        descripcion = product.descripcion,
                        precio = product.precio,
                        stock = product.stock,
                        imagenUrl = product.imagenUrl,
                        cantidad = finalQty
                    )
                )
            }
        }
    }

    /**
     * Disminuye la cantidad o elimina el item del carrito si llega a 0.
     */
    fun updateCartQuantity(product: Product, targetQty: Int) {
        viewModelScope.launch {
            if (targetQty <= 0) {
                cartDao.deleteItem(
                    CartEntity(
                        id = product.id,
                        sellerId = product.sellerId,
                        nombre = product.nombre,
                        descripcion = product.descripcion,
                        precio = product.precio,
                        stock = product.stock,
                        imagenUrl = product.imagenUrl,
                        cantidad = 1
                    )
                )
            } else {
                val finalQty = if (targetQty > product.stock) product.stock else targetQty
                cartDao.insertOrUpdate(
                    CartEntity(
                        id = product.id,
                        sellerId = product.sellerId,
                        nombre = product.nombre,
                        descripcion = product.descripcion,
                        precio = product.precio,
                        stock = product.stock,
                        imagenUrl = product.imagenUrl,
                        cantidad = finalQty
                    )
                )
            }
        }
    }

    /**
     * Remueve por completo un item del carrito.
     */
    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            cartDao.deleteItem(CartEntity.fromDomain(item))
        }
    }

    /**
     * Limpia la base de datos de Room.
     */
    fun clearCart() {
        viewModelScope.launch {
            cartDao.clearCart()
        }
    }

    // ==================================================
    // MÓDULO 6: GEOLOCALIZACIÓN FÍSICA (Google Play Location)
    // ==================================================

    /**
     * Captura las coordenadas físicas exactas de latitud y longitud
     * utilizando FusedLocationProviderClient.
     */
    @SuppressLint("MissingPermission")
    fun fetchUserLocation() {
        _checkoutState.value = CheckoutState.Locating
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    _latitude.value = location.latitude
                    _longitude.value = location.longitude
                    _checkoutState.value = CheckoutState.Idle
                } else {
                    // Fallback a última ubicación conocida si la actual falla
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc ->
                        if (lastLoc != null) {
                            _latitude.value = lastLoc.latitude
                            _longitude.value = lastLoc.longitude
                            _checkoutState.value = CheckoutState.Idle
                        } else {
                            _checkoutState.value = CheckoutState.Error("No se pudo obtener señal de satélite GPS. Intente de nuevo.")
                        }
                    }
                }
            }.addOnFailureListener { e ->
                _checkoutState.value = CheckoutState.Error("Fallo de hardware de ubicación: ${e.localizedMessage}")
            }
        } catch (e: SecurityException) {
            _checkoutState.value = CheckoutState.Error("Permisos de ubicación denegados en el dispositivo.")
        }
    }

    // ==================================================
    // MÓDULO 7: PROCESAMIENTO DE ORDEN Y PAGO EPAYCO
    // ==================================================

    /**
     * Ejecuta el Checkout completo:
     * 1. Envía la orden a PostgreSQL con la geolocalización física.
     * 2. Simula la pasarela de pagos ePayco y dispara el webhook de confirmación.
     * 3. Limpia la base de datos local Room tras la compra aprobada.
     */
    fun performCheckout(totalPrice: Double) {
        val items = cartItems.value
        if (items.isEmpty()) {
            _checkoutState.value = CheckoutState.Error("El carrito está vacío.")
            return
        }

        _checkoutState.value = CheckoutState.Loading
        viewModelScope.launch {
            // Paso 1: Registrar orden en PostgreSQL con coordenadas GPS capturadas
            orderRepository.createOrder(
                total = totalPrice,
                latitud = _latitude.value,
                longitud = _longitude.value,
                items = items
            ).onSuccess { message ->
                // Supongamos que el backend nos da un mensaje de éxito. Consultamos la última orden para pagar
                val buyer = authRepository.getLoggedUser()
                if (buyer != null) {
                    // Consultamos el historial para obtener el ID de la orden recién creada
                    orderRepository.getBuyerOrderHistory(buyer.id).onSuccess { orderList ->
                        val latestOrder = orderList.firstOrNull() // La primera de la lista por orden descendente o id
                        if (latestOrder != null) {
                            // Paso 2: Simulación de ePayco (Transacción aprobada de manera robusta)
                            val simulatedTxId = "TX-EPAYCO-" + System.currentTimeMillis()
                            orderRepository.processPayment(
                                orderId = latestOrder.id,
                                transactionId = simulatedTxId,
                                status = "Aprobado"
                            ).onSuccess { payMessage ->
                                // Paso 3: Limpiar el carrito de Room localmente
                                cartDao.clearCart()
                                _checkoutState.value = CheckoutState.Success("¡Compra Realizada con éxito!\n$payMessage\nID Transacción: $simulatedTxId")
                                loadOrderHistory() // Recargar historial de compras
                            }.onFailure { err ->
                                _checkoutState.value = CheckoutState.Error("Fallo en pasarela ePayco: ${err.localizedMessage}")
                            }
                        } else {
                            _checkoutState.value = CheckoutState.Error("No se pudo recuperar la referencia de la orden")
                        }
                    }.onFailure { err ->
                        _checkoutState.value = CheckoutState.Error("Error al validar historial de órdenes: ${err.localizedMessage}")
                    }
                } else {
                    _checkoutState.value = CheckoutState.Error("Usuario no autenticado")
                }
            }.onFailure { error ->
                _checkoutState.value = CheckoutState.Error(
                    error.localizedMessage ?: "Error al registrar la orden en el servidor."
                )
            }
        }
    }

    /**
     * Carga el historial de compras del usuario logueado.
     */
    fun loadOrderHistory() {
        val buyer = authRepository.getLoggedUser()
        if (buyer == null) {
            _orderHistoryState.value = OrderHistoryState.Error("Sesión de usuario no encontrada")
            return
        }

        _orderHistoryState.value = OrderHistoryState.Loading
        viewModelScope.launch {
            orderRepository.getBuyerOrderHistory(buyer.id)
                .onSuccess { list ->
                    _orderHistoryState.value = OrderHistoryState.Success(list)
                }
                .onFailure { error ->
                    _orderHistoryState.value = OrderHistoryState.Error(
                        error.localizedMessage ?: "Error al obtener historial de compras"
                    )
                }
        }
    }

    fun clearCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}

// Estados del Catálogo de Productos
sealed interface CatalogState {
    object Loading : CatalogState
    data class Success(val products: List<Product>) : CatalogState
    data class Error(val message: String) : CatalogState
}

// Estados de la Operación de Checkout y Geolocalización
sealed interface CheckoutState {
    object Idle : CheckoutState
    object Locating : CheckoutState
    object Loading : CheckoutState
    data class Success(val message: String) : CheckoutState
    data class Error(val message: String) : CheckoutState
}

// Estados de Carga de Historial de Órdenes
sealed interface OrderHistoryState {
    object Loading : OrderHistoryState
    data class Success(val orders: List<Order>) : OrderHistoryState
    data class Error(val message: String) : OrderHistoryState
}
