package com.example.notehub.data

import com.example.notehub.data.remote.LoginRequest
import com.example.notehub.data.remote.RegisterRequest
import com.example.notehub.data.remote.RetrofitClient
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
 * AuthService — Handles user authentication against the PHP/MySQL backend.
 */
object AuthService {

    /**
     * Authenticates user against the PHP database.
     */
    suspend fun login(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val response = RetrofitClient.api.login(LoginRequest(email, password))
            if (response.success) {
                AuthResult.Success
            } else {
                AuthResult.Error(response.message)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Network connection failed. Verify your server is online.")
        }
    }

    /**
     * Registers a new user.
     */
    suspend fun signUp(fullName: String, email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            // Generate a username from the email prefix
            val username = email.substringBefore("@").take(15)
            val response = RetrofitClient.api.register(RegisterRequest(username, email, password, fullName))
            if (response.success) {
                AuthResult.Success
            } else {
                AuthResult.Error(response.message)
            }
        } catch (e: Exception) {
            AuthResult.Error(e.localizedMessage ?: "Registration failed. Server offline.")
        }
    }
}
