package com.example.notehub.data

import android.util.Log
import com.example.notehub.data.remote.DataSettings
import com.example.notehub.data.remote.LoginRequest
import com.example.notehub.data.remote.RegisterRequest
import com.example.notehub.data.remote.RetrofitClient
import com.example.notehub.data.remote.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Result of an authentication operation.
 */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Loading : AuthResult()
}

/**
 * AuthService — Handles user authentication with automatic online/offline detection.
 *
 * When the device is ONLINE  → authenticates against the AWS Laravel backend.
 * When the device is OFFLINE → allows entry in offline (read-only cache) mode.
 *
 * FIX: Now correctly handles account switching:
 *   - Registers on AWS website  → then logs in on mobile app with that email  → loads THAT user's data.
 *   - Prevents old cached account data from leaking into a newly-authenticated session.
 */
object AuthService {

    /**
     * Authenticates the user.
     *
     * Online:  validates credentials with the AWS Laravel backend.
     * Offline: skips server call and returns Success so the user can
     *          still view their locally-cached notes.
     *
     * IMPORTANT: When online, the server MUST return a valid token for the entered
     * email/password — there is no "skip" for online mode. This ensures that a user
     * who registered on the website and tries to log in on the mobile app gets
     * their own account, not someone else's cached session.
     */
    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        // Auto offline-mode: skip server call when there's no internet
        if (DataSettings.isOfflineMode()) {
            Log.d("AuthService", "Offline mode — skipping server auth")
            return@withContext AuthResult.Success
        }

        // Online: validate credentials against the SSP API.
        // The token is saved inside RetrofitClient's interceptor upon a 200 response.
        try {
            val response = RetrofitClient.api.login(LoginRequest(email, password))
            if (response.success) {
                // TokenManager.saveUser() was already called inside RetrofitClient's interceptor.
                // If it returned true, that means a DIFFERENT user just logged in —
                // the interceptor logs a warning; the ViewModel will re-fetch fresh data.
                Log.d("AuthService", "Login successful for: $email")
                AuthResult.Success
            } else {
                Log.w("AuthService", "Login rejected by server: ${response.message}")
                AuthResult.Error(response.message)
            }
        } catch (e: Exception) {
            Log.e("AuthService", "Login network error", e)
            AuthResult.Error(
                e.localizedMessage
                    ?: "Cannot reach the server. Please check your internet connection."
            )
        }
    }

    /**
     * Registers a new user.
     *
     * Online:  sends registration data to the AWS Laravel backend.
     * Offline: registration is not possible — returns an informative error.
     */
    suspend fun signUp(fullName: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            if (DataSettings.isOfflineMode()) {
                // Cannot register without a server connection
                return@withContext AuthResult.Error(
                    "You are offline. Please connect to the internet to create an account."
                )
            }

            try {
                val username = email.substringBefore("@").take(15)
                val response = RetrofitClient.api.register(
                    RegisterRequest(username, email, password, fullName)
                )
                if (response.success) {
                    Log.d("AuthService", "Registration successful for: $email")
                    AuthResult.Success
                } else {
                    AuthResult.Error(response.message)
                }
            } catch (e: Exception) {
                AuthResult.Error(
                    e.localizedMessage
                        ?: "Registration failed. Please check your internet connection."
                )
            }
        }

    /**
     * Logs the user out — clears token AND user identity from SharedPreferences.
     * This ensures the next login always authenticates from scratch.
     */
    fun logout() {
        TokenManager.clearAll()
        Log.d("AuthService", "User logged out — token and session cleared")
    }
}
