package com.example.ecomers.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomers.domain.model.User
import com.example.ecomers.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel de Autenticación: AuthViewModel
 * 
 * Gestiona el estado de Login, Registro, Recuperación de contraseña,
 * y activa el Bypass Biométrico (Módulo 1 y 2).
 * 
 * Expone flujos reactivos unidireccionales (UDF) mediante [StateFlow]
 * garantizando que la UI sea una función del estado.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // --- ESTADOS DE LA UI ---
    private val _loginState = MutableStateFlow<AuthState>(AuthState.Idle)
    val loginState: StateFlow<AuthState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthState>(AuthState.Idle)
    val registerState: StateFlow<AuthState> = _registerState.asStateFlow()

    private val _recoverState = MutableStateFlow<RecoverState>(RecoverState.Idle)
    val recoverState: StateFlow<RecoverState> = _recoverState.asStateFlow()

    // Flag reactivo para controlar el interruptor (Switch) de huella en la UI
    private val _isBiometricsEnabled = MutableStateFlow(false)
    val isBiometricsEnabled: StateFlow<Boolean> = _isBiometricsEnabled.asStateFlow()

    init {
        // Cargar estado inicial de la huella dactilar al arrancar el ViewModel
        _isBiometricsEnabled.value = authRepository.isBiometricsEnabled()
    }

    /**
     * Intenta autenticar al usuario contra el servidor PostgreSQL mediante credenciales.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = AuthState.Error("Por favor, rellene todos los campos")
            return
        }

        _loginState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    _loginState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _loginState.value = AuthState.Error(
                        error.localizedMessage ?: "Error de red. Verifique que el servidor backend esté encendido."
                    )
                }
        }
    }

    /**
     * Intenta registrar un nuevo usuario con rol explícito.
     */
    fun register(email: String, fullName: String, password: String, role: String) {
        if (email.isBlank() || fullName.isBlank() || password.isBlank() || role.isBlank()) {
            _registerState.value = AuthState.Error("Por favor, rellene todos los campos")
            return
        }

        _registerState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(email, password, fullName, role)
                .onSuccess { user ->
                    _registerState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _registerState.value = AuthState.Error(
                        error.localizedMessage ?: "Error en el registro de usuario."
                    )
                }
        }
    }

    /**
     * Envía petición para recuperación de contraseña.
     */
    fun recoverPassword(email: String) {
        if (email.isBlank()) {
            _recoverState.value = RecoverState.Error("El correo electrónico es obligatorio")
            return
        }

        _recoverState.value = RecoverState.Loading
        viewModelScope.launch {
            authRepository.recoverPassword(email)
                .onSuccess { msg ->
                    _recoverState.value = RecoverState.Success(msg)
                }
                .onFailure { error ->
                    _recoverState.value = RecoverState.Error(error.localizedMessage ?: "Error al recuperar cuenta")
                }
        }
    }

    /**
     * Habilita/Deshabilita la opción de autenticación biométrica en el dispositivo.
     */
    fun toggleBiometrics(enabled: Boolean) {
        authRepository.setBiometricsEnabled(enabled)
        _isBiometricsEnabled.value = enabled
        if (!enabled) {
            // Si lo desactiva, limpiamos las credenciales cacheadas por seguridad
            authRepository.saveEncryptedCredentials("")
        }
    }

    /**
     * MÓDULO 2: Bypass Biométrico (Login con Huella)
     * 
     * Si la huella digital es verificada con éxito en el dispositivo físico,
     * este método realiza un bypass del login tradicional leyendo la contraseña
     * guardada de forma encriptada en EncryptedSharedPreferences y logueándolo automáticamente.
     */
    fun performBiometricLoginBypass() {
        val loggedUser = authRepository.getLoggedUser()
        val savedPassword = authRepository.getEncryptedCredentials()

        if (loggedUser != null && !savedPassword.isNullOrBlank()) {
            _loginState.value = AuthState.Loading
            viewModelScope.launch {
                authRepository.login(loggedUser.email, savedPassword)
                    .onSuccess { user ->
                        _loginState.value = AuthState.Success(user)
                    }
                    .onFailure { error ->
                        _loginState.value = AuthState.Error("Bypass fallido: " + error.localizedMessage)
                    }
            }
        } else {
            _loginState.value = AuthState.Error("No hay credenciales biométricas configuradas. Inicie sesión con contraseña primero.")
        }
    }

    /**
     * Obtiene el token actual para la redirección inicial en el Splash Screen.
     */
    fun getInitialNavigationTarget(): InitialTarget {
        val token = authRepository.getSessionToken()
        val user = authRepository.getLoggedUser()

        if (token.isNullOrBlank() || user == null) {
            return InitialTarget.LOGIN
        }

        return when (user.rol) {
            com.example.ecomers.domain.model.UserRole.ADMIN -> InitialTarget.ADMIN_DASHBOARD
            com.example.ecomers.domain.model.UserRole.SELLER -> InitialTarget.SELLER_DASHBOARD
            com.example.ecomers.domain.model.UserRole.BUYER -> InitialTarget.BUYER_DASHBOARD
        }
    }

    /**
     * Cierra la sesión activa del usuario.
     */
    fun logout() {
        authRepository.logout()
        _loginState.value = AuthState.Idle
    }

    fun clearErrors() {
        _loginState.value = AuthState.Idle
        _registerState.value = AuthState.Idle
        _recoverState.value = RecoverState.Idle
    }
}

// Estados del Flujo de Autenticación
sealed interface AuthState {
    object Idle : AuthState
    object Loading : AuthState
    data class Success(val user: User) : AuthState
    data class Error(val message: String) : AuthState
}

// Estados del Flujo de Recuperación
sealed interface RecoverState {
    object Idle : RecoverState
    object Loading : RecoverState
    data class Success(val message: String) : RecoverState
    data class Error(val message: String) : RecoverState
}

// Destinos Iniciales para el Splash Screen
enum class InitialTarget {
    LOGIN,
    ADMIN_DASHBOARD,
    SELLER_DASHBOARD,
    BUYER_DASHBOARD
}
