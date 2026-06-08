package com.example.notehub.data

import android.util.Log
import com.example.notehub.data.remote.DataSettings
import com.example.notehub.data.remote.LoginRequest
import com.example.notehub.data.remote.RegisterRequest
import com.example.notehub.data.remote.RetrofitClient
import com.example.notehub.data.remote.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Result of an authentication operation
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Loading : AuthResult()
}

// Handles user authentication with online/offline detection
object AuthService {

    // Authenticates the user
    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        if (DataSettings.isOfflineMode()) {
            Log.d("AuthService", "Offline mode — validating against cached credentials")

            val cachedEmail = TokenManager.getLoggedInEmail()

            // No cached user at all — must go online to log in for the first time
            if (cachedEmail.isNullOrEmpty()) {
                return@withContext AuthResult.Error(
                    "You have no offline data. Please connect to the internet to log in."
                )
            }

            // Cached email doesn't match what was entered — block access
            if (cachedEmail.lowercase() != email.trim().lowercase()) {
                return@withContext AuthResult.Error(
                    "You are offline. Only the last logged-in account ($cachedEmail) can be accessed without internet."
                )
            }

            // Email matches the cached account — allow offline access
            return@withContext AuthResult.Success
        }

        try {
            val response = RetrofitClient.api.login(LoginRequest(email, password))
            if (response.success) {
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

    // Registers a new user
    suspend fun signUp(fullName: String, email: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            if (DataSettings.isOfflineMode()) {
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

    // Logs the user out
    fun logout() {
        TokenManager.clearAll()
        Log.d("AuthService", "User logged out")
    }
}
