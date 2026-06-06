package com.example.notehub.data.remote

import android.content.Context
import android.content.SharedPreferences

/**
 * TokenManager — Persists the JWT token AND the logged-in user's email
 * in SharedPreferences so authentication survives app restarts.
 *
 * This fixes the bug where registering on the AWS website then logging in
 * on the mobile app would load a previously-cached account instead of the
 * newly authenticated one.
 */
object TokenManager {

    private const val PREFS_NAME  = "auth_prefs"
    private const val KEY_TOKEN   = "jwt_token"
    private const val KEY_USER_EMAIL = "logged_in_email"
    private const val KEY_USER_NAME  = "logged_in_name"
    private const val KEY_USER_ID    = "logged_in_user_id"

    private var prefs: SharedPreferences? = null

    /** Must be called once from MainActivity.onCreate() before any other use. */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ── Token ─────────────────────────────────────────────────────────────

    fun saveToken(token: String) {
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    fun clearToken() {
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    fun isAuthenticated(): Boolean = !getToken().isNullOrEmpty()

    // ── User identity ──────────────────────────────────────────────────────

    /**
     * Saves the identity of the successfully-logged-in user.
     * Returns true if this is a *different* user than was previously logged in
     * (meaning old cached data should be cleared).
     */
    fun saveUser(email: String, name: String = "", userId: Int = 0): Boolean {
        val previousEmail = getLoggedInEmail()
        val isAccountSwitch = previousEmail != null &&
                previousEmail.isNotEmpty() &&
                previousEmail.lowercase() != email.lowercase()

        prefs?.edit()
            ?.putString(KEY_USER_EMAIL, email.lowercase())
            ?.putString(KEY_USER_NAME, name)
            ?.putInt(KEY_USER_ID, userId)
            ?.apply()

        return isAccountSwitch
    }

    fun getLoggedInEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)

    fun getLoggedInName(): String? = prefs?.getString(KEY_USER_NAME, null)

    fun getLoggedInUserId(): Int = prefs?.getInt(KEY_USER_ID, 0) ?: 0

    /** Clears ALL auth data — called on logout. */
    fun clearAll() {
        prefs?.edit()
            ?.remove(KEY_TOKEN)
            ?.remove(KEY_USER_EMAIL)
            ?.remove(KEY_USER_NAME)
            ?.remove(KEY_USER_ID)
            ?.apply()
    }
}
