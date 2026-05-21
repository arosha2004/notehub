package com.example.notehub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Auth Screens (no icon needed)
    data object Login : Screen("login", "Login")
    data object SignUp : Screen("signup", "Sign Up")
    
    // Main App Screens (with navigation drawer items)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    data object Notes : Screen("notes", "All Notes", Icons.AutoMirrored.Filled.Note)
    data object LocationNotes : Screen("location_notes", "Location Notes", Icons.Filled.LocationOn)
    data object Uploads : Screen("uploads", "Uploads", Icons.Filled.Upload)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    
    // Feature Screens (no icon needed for navigation)
    data object AddNote : Screen("add_note", "Add Note")
    data object NoteDetail : Screen("note_detail/{noteId}", "Note Details")
    data object AddLocationNote : Screen("add_location_note", "Add Location Note")
    data object LocationNoteDetail : Screen("location_note_detail/{noteId}", "Location Note Details")
    
    companion object {
        // List of screens to show in the navigation drawer
        val drawerScreens = listOf(
            Dashboard,
            Notes,
            Uploads,
            Settings
        )
        
        fun shouldShowDrawer(route: String?): Boolean {
            return route != Login.route && route != SignUp.route && 
                   route != AddNote.route && route?.startsWith("note_detail") != true &&
                   route != AddLocationNote.route && route?.startsWith("location_note_detail") != true
        }
    }
}
