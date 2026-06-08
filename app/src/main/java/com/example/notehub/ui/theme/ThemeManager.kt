package com.example.notehub.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ThemeManager — Central controller for global app theme state.
 * Supports persistence via SharedPreferences and automatic fallback to system setting.
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_settings_prefs"
    private const val KEY_THEME = "app_theme"
    private const val KEY_MANUAL_PREF = "has_manual_pref"
    private const val KEY_DARK_MODE = "is_dark_mode"

    private var prefs: SharedPreferences? = null

    // THEME SELECTION STATE
    private var _currentTheme by mutableStateOf(AppTheme.INDIGO)
    val currentTheme: AppTheme
        get() = _currentTheme

    // DARK MODE STATE
    private var _hasUserSetDarkModeManualPref by mutableStateOf(false)
    val hasUserSetDarkModeManualPref: Boolean
        get() = _hasUserSetDarkModeManualPref

    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean
        get() = _isDarkMode

    /**
     * Initializes theme preferences from storage.
     */
    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _hasUserSetDarkModeManualPref = prefs?.getBoolean(KEY_MANUAL_PREF, false) ?: false
        _isDarkMode = prefs?.getBoolean(KEY_DARK_MODE, false) ?: false
        val savedTheme = prefs?.getString(KEY_THEME, AppTheme.INDIGO.name) ?: AppTheme.INDIGO.name
        _currentTheme = try {
            AppTheme.valueOf(savedTheme)
        } catch (e: Exception) {
            AppTheme.INDIGO
        }
    }

    /**
     * Changes the color palette (INDIGO or OCEAN).
     */
    fun setTheme(theme: AppTheme) {
        _currentTheme = theme
        prefs?.edit()?.putString(KEY_THEME, theme.name)?.apply()
    }

    /**
     * Flips the dark mode state to the opposite of what it currently is.
     */
    fun toggleDarkMode() {
        setDarkMode(!_isDarkMode)
    }

    /**
     * Explicitly sets dark mode ON (true) or OFF (false).
     */
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
        _hasUserSetDarkModeManualPref = true
        prefs?.edit()?.apply {
            putBoolean(KEY_MANUAL_PREF, true)
            putBoolean(KEY_DARK_MODE, enabled)
        }?.apply()
    }

    /**
     * Resets theme tracking to follow device settings.
     */
    fun resetToSystemDefault() {
        _hasUserSetDarkModeManualPref = false
        prefs?.edit()?.putBoolean(KEY_MANUAL_PREF, false)?.apply()
    }
}

/**
 * AppTheme — Defines the available color sets for the app.
 */
enum class AppTheme {
    INDIGO,  // The default Purple/Indigo look
    OCEAN    // An alternate Blue/Teal look
}

