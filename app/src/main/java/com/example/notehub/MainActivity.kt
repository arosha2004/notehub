package com.example.notehub

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notehub.data.remote.DataSettings
import com.example.notehub.data.remote.TokenManager
import com.example.notehub.ui.theme.NoteHubTheme
import com.example.notehub.ui.theme.ThemeManager
import com.example.notehub.utils.NetworkMonitor

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialise managers — order matters: NetworkMonitor first
        NetworkMonitor.init(this)
        DataSettings.init(this)
        TokenManager.init(this)   // must be before any auth call so prefs are ready
        ThemeManager.init(this)
        enableEdgeToEdge()
        setContent {
            NoteHubTheme {
                MainScreen()
            }
        }
    }
}