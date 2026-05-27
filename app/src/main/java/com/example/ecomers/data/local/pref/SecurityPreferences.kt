package com.example.ecomers.data.local.pref

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persistencia Local Segura: SecurityPreferences
 * 
 * Almacena el Token JWT de autenticación y metadatos sensibles de manera cifrada
 * en el almacenamiento privado del dispositivo físico.
 * 
 * Utiliza algoritmos AES-256 (SIV para llaves y GCM para valores) en conjunto
 * con la librería AndroidX Security Crypto.
 */
@Singleton
class SecurityPreferences @Inject constructor(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_ecomers_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_USER_ROLE = "user_role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_BIOMETRICS_ENABLED = "biometrics_enabled"
        private const val KEY_ENCRYPTED_PASSWORD = "encrypted_password"
    }

    /**
     * Guarda el token JWT tras un inicio de sesión exitoso.
     */
    fun saveToken(token: String) {
        sharedPrefs.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    /**
     * Obtiene el token JWT actual. Retorna nulo si no ha iniciado sesión.
     */
    fun getToken(): String? {
        return sharedPrefs.getString(KEY_JWT_TOKEN, null)
    }

    /**
     * Guarda la información básica del usuario logueado.
     */
    fun saveUserData(userId: Int, name: String, email: String, role: String) {
        sharedPrefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getUserId(): Int {
        return sharedPrefs.getInt(KEY_USER_ID, -1)
    }

    fun getUserName(): String? {
        return sharedPrefs.getString(KEY_USER_NAME, null)
    }

    fun getUserEmail(): String? {
        return sharedPrefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserRole(): String? {
        return sharedPrefs.getString(KEY_USER_ROLE, null)
    }

    /**
     * Gestión del estado de la autenticación biométrica (Módulo 2)
     */
    fun setBiometricsEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean(KEY_BIOMETRICS_ENABLED, enabled).apply()
    }

    fun isBiometricsEnabled(): Boolean {
        return sharedPrefs.getBoolean(KEY_BIOMETRICS_ENABLED, false)
    }

    /**
     * Guarda temporalmente la contraseña cifrada localmente para realizar el bypass
     * seguro del Login tras la autenticación biométrica exitosa.
     */
    fun saveEncryptedCredentials(password: String) {
        sharedPrefs.edit().putString(KEY_ENCRYPTED_PASSWORD, password).apply()
    }

    fun getEncryptedCredentials(): String? {
        return sharedPrefs.getString(KEY_ENCRYPTED_PASSWORD, null)
    }

    /**
     * Cierra la sesión limpiando todas las llaves de seguridad y tokens del dispositivo.
     */
    fun clearSession() {
        sharedPrefs.edit()
            .remove(KEY_JWT_TOKEN)
            .remove(KEY_USER_ROLE)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            // Conservamos el flag de biometría para habilitarlo o no, pero borramos credenciales
            .remove(KEY_ENCRYPTED_PASSWORD)
            .apply()
    }
}
