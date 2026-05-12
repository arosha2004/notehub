package com.example.notehub.data

import kotlinx.coroutines.delay

/**
 * Result of an authentication operation.
 */
sealed class AuthResult {
    data object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    data object Loading : AuthResult()
}

/**
 * AuthService — A service that handles user authentication.
 * Currently implemented as a Mock service for demonstration.
 */
object AuthService {

    /**
     * Simulates a login request.
     * 
     * @param email The user's email
     * @param password The user's password
     * @return AuthResult indicating success or failure
     */
    suspend fun login(email: String, password: String): AuthResult {
        // Simulate network delay
        delay(1500)

        // Mock validation
        return if (email.contains("@") && password.length >= 6) {
            AuthResult.Success
        } else if (!email.contains("@")) {
            AuthResult.Error("Please enter a valid email address.")
        } else {
            AuthResult.Error("Password must be at least 6 characters.")
        }
    }

    /**
     * Simulates a sign-up request.
     */
    suspend fun signUp(fullName: String, email: String, password: String): AuthResult {
        // Simulate network delay
        delay(2000)

        return if (fullName.isNotBlank() && email.contains("@") && password.length >= 6) {
            AuthResult.Success
        } else if (fullName.isBlank()) {
            AuthResult.Error("Full name cannot be empty.")
        } else if (!email.contains("@")) {
            AuthResult.Error("Invalid email format.")
        } else {
            AuthResult.Error("Password too short.")
        }
    }
}
