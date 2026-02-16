package com.example.notehub.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object ThemeManager {
    private var _currentTheme by mutableStateOf(AppTheme.INDIGO)
    val currentTheme: AppTheme
        get() = _currentTheme
    
    private var _isDarkMode by mutableStateOf(false)
    val isDarkMode: Boolean
        get() = _isDarkMode
    
    fun setTheme(theme: AppTheme) {
        _currentTheme = theme
    }
    
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
    }
    
    fun setDarkMode(enabled: Boolean) {
        _isDarkMode = enabled
    }
}

enum class AppTheme {
    INDIGO,  // Purple/Indigo theme (default)
    OCEAN    // Blue/Teal theme
}
