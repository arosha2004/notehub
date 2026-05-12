// ============================================================
// MainActivity.kt
// ------------------------------------------------------------
// This is the ENTRY POINT of the entire NoteHub application.
// When the user taps the app icon, Android launches THIS file first.
//
// It only does 3 things:
//  1. Enables Edge-to-Edge display (content fills the full screen)
//  2. Applies the NoteHubTheme (colors, dark mode)
//  3. Launches MainScreen() (the app shell with navigation)
// ============================================================

package com.example.notehub // declares this file belongs to the notehub app package

import android.os.Bundle                             // Bundle holds the saved state of the activity
import androidx.activity.ComponentActivity           // base class for all Jetpack Compose activities
import androidx.activity.compose.setContent          // function to set the Compose UI as the screen content
import androidx.activity.enableEdgeToEdge            // makes the UI extend behind status & navigation bars
import com.example.notehub.ui.theme.NoteHubTheme    // our custom theme (colors, dark mode, typography)

// MainActivity inherits from ComponentActivity
// ComponentActivity is the Compose-compatible version of the older AppCompatActivity
class MainActivity : ComponentActivity() {

    // onCreate() is called by Android when the app is first launched
    // savedInstanceState holds any previously saved data (used after rotation, etc.)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState) // always call the parent's onCreate first — required by Android

        // Makes the app content draw behind the system status bar and navigation bar
        // This gives a full-screen, immersive look (no white/grey bar gaps)
        enableEdgeToEdge()

        // setContent{} replaces the old XML layout system
        // Everything inside {} is our Jetpack Compose UI tree
        setContent {

            // NoteHubTheme wraps the entire app with our color scheme
            // It checks ThemeManager.isDarkMode to decide Light or Dark colors
            // If the user toggled Dark Mode in Settings, this automatically applies it
            NoteHubTheme {

                // MainScreen is the app shell — it contains:
                //  - The Top App Bar (title)
                //  - The Bottom Navigation Bar (Dashboard, Notes, Uploads, Settings)
                //  - The NavHost (swaps screens based on navigation)
                MainScreen()
            }
        }
    }
}