package com.example.notehub.data.remote

/**
 * TokenManager — Simple token store that holds the JWT returned by
 * the Laravel JWT backend. Used by Retrofit's AuthInterceptor.
 */
object TokenManager {
    private var jwtToken: String? = null

    /**
     * Cache token.
     */
    fun saveToken(token: String) {
        jwtToken = token
    }

    /**
     * Fetch active token.
     */
    fun getToken(): String? {
        return jwtToken
    }

    /**
     * Clear token on signout.
     */
    fun clearToken() {
        jwtToken = null
    }

    /**
     * Checks if user is authenticated via JWT.
     */
    fun isAuthenticated(): Boolean {
        return jwtToken != null
    }
}
