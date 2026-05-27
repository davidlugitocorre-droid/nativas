package com.example.ecomers.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomers.domain.model.User
import com.example.ecomers.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel del Administrador: AdminViewModel
 * 
 * Orquesta y administra los estados de la UI del Módulo 3 (CRUD de Usuarios).
 * Consume los endpoints correspondientes al rol de Administrador sobre PostgreSQL.
 */
@HiltViewModel
class AdminViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    // --- ESTADOS DE LA UI ---
    
    // Listado de usuarios
    private val _usersState = MutableStateFlow<UsersListState>(UsersListState.Loading)
    val usersState: StateFlow<UsersListState> = _usersState.asStateFlow()

    // Operación CRUD (Creación / Edición / Eliminación)
    private val _crudState = MutableStateFlow<CrudState>(CrudState.Idle)
    val crudState: StateFlow<CrudState> = _crudState.asStateFlow()

    // Detalle de un usuario específico cargado para edición
    private val _selectedUserState = MutableStateFlow<UserDetailState>(UserDetailState.Idle)
    val selectedUserState: StateFlow<UserDetailState> = _selectedUserState.asStateFlow()

    init {
        loadUsers()
    }

    /**
     * Consume el endpoint GET /api/users para listar los registros de PostgreSQL.
     */
    fun loadUsers() {
        _usersState.value = UsersListState.Loading
        viewModelScope.launch {
            userRepository.getUsers()
                .onSuccess { list ->
                    _usersState.value = UsersListState.Success(list)
                }
                .onFailure { error ->
                    _usersState.value = UsersListState.Error(
                        error.localizedMessage ?: "Error al obtener usuarios"
                    )
                }
        }
    }

    /**
     * Consume el endpoint POST /api/users para insertar un nuevo usuario con rol.
     */
    fun createUser(email: String, fullName: String, password: String, role: String) {
        _crudState.value = CrudState.Loading
        viewModelScope.launch {
            userRepository.createUser(email, password, fullName, role)
                .onSuccess {
                    _crudState.value = CrudState.Success("Usuario creado correctamente")
                    loadUsers() // Recargar lista
                }
                .onFailure { error ->
                    _crudState.value = CrudState.Error(error.localizedMessage ?: "Error al crear usuario")
                }
        }
    }

    /**
     * Consume el endpoint GET /api/users/:id para cargar la información de edición.
     */
    fun loadUserDetails(userId: Int) {
        if (userId == -1) {
            _selectedUserState.value = UserDetailState.Idle
            return
        }
        
        _selectedUserState.value = UserDetailState.Loading
        viewModelScope.launch {
            userRepository.getUserDetails(userId)
                .onSuccess { user ->
                    _selectedUserState.value = UserDetailState.Success(user)
                }
                .onFailure { error ->
                    _selectedUserState.value = UserDetailState.Error(error.localizedMessage ?: "Error al cargar usuario")
                }
        }
    }

    /**
     * Consume el endpoint PUT /api/users/:id para actualizar los datos del usuario.
     */
    fun updateUser(userId: Int, email: String, fullName: String, role: String) {
        _crudState.value = CrudState.Loading
        viewModelScope.launch {
            userRepository.updateUser(userId, email, fullName, role)
                .onSuccess {
                    _crudState.value = CrudState.Success("Usuario actualizado correctamente")
                    loadUsers() // Recargar lista
                }
                .onFailure { error ->
                    _crudState.value = CrudState.Error(error.localizedMessage ?: "Error al actualizar usuario")
                }
        }
    }

    /**
     * Consume el endpoint DELETE /api/users/:id para borrar el usuario de PostgreSQL.
     */
    fun deleteUser(userId: Int) {
        _crudState.value = CrudState.Loading
        viewModelScope.launch {
            userRepository.deleteUser(userId)
                .onSuccess {
                    _crudState.value = CrudState.Success("Usuario eliminado de la base de datos")
                    loadUsers() // Recargar lista
                }
                .onFailure { error ->
                    _crudState.value = CrudState.Error(error.localizedMessage ?: "Error al eliminar usuario")
                }
        }
    }

    fun clearCrudState() {
        _crudState.value = CrudState.Idle
    }
}

// Representa el estado de carga de la lista de usuarios
sealed interface UsersListState {
    object Loading : UsersListState
    data class Success(val users: List<User>) : UsersListState
    data class Error(val message: String) : UsersListState
}

// Representa el estado de una acción de escritura CRUD
sealed interface CrudState {
    object Idle : CrudState
    object Loading : CrudState
    data class Success(val message: String) : CrudState
    data class Error(val message: String) : CrudState
}

// Representa el detalle cargado de un usuario específico
sealed interface UserDetailState {
    object Idle : UserDetailState
    object Loading : UserDetailState
    data class Success(val user: User) : UserDetailState
    data class Error(val message: String) : UserDetailState
}
