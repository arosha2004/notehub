package com.example.notehub.ui.theme

// ============================================================
// ThemeManager.kt
// ------------------------------------------------------------
// This file acts as the "BRAIN" for the app's visual appearance.
//
// It uses the Singleton Pattern: 'object ThemeManager' means there
// is only ONE instance of this in the entire app.
//
// Every screen in NoteHub (Login, Dashboard, Settings) talks to
// this manager to know whether it should show Light or Dark mode.
// ============================================================

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ThemeManager — Central controller for global app theme state.
 */
object ThemeManager {
    
    // ── THEME SELECTION STATE ──────────────────────────────────────
    // mutableStateOf means this variable is "Observable".
    // When _currentTheme changes, Compose instantly redraws every UI
    // element that uses it!
    private var _currentTheme by mutableStateOf(AppTheme.INDIGO)
    
    // Public getter — screens can read the theme but can't change it directly
    val currentTheme: AppTheme
        get() = _currentTheme
    
    // ── DARK MODE STATE ───────────────────────────────────────────
    // Controls the global Light/Dark mode toggle.
    // Default is false (Light Mode).
    private var _isDarkMode by mutableStateOf(false)
    
    // Public getter
    val isDarkMode: Boolean
        get() = _isDarkMode
    
    // ── ACTIONS ────────────────────────────────────────────────────

    /**
     * Changes the color palette (INDIGO or OCEAN).
     */
    fun setTheme(theme: AppTheme) {
        _currentTheme = theme
    }
    
    /**
     * Flips the dark mode state to the opposite of what it currently is.
     */
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    /**
     * Explicitly sets dark mode ON (true) or OFF (false).
     * Used by the Switch in the Settings screen.
     */
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
}

/**
 * AppTheme — Defines the available color sets for the app.
 */
enum class AppTheme {
    INDIGO,  // The default Purple/Indigo look
    OCEAN    // An alternate Blue/Teal look
}
