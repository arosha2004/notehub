package com.example.notehub
// MainScreen.kt
// ------------------------------------------------------------
// This is the APP SHELL — the permanent container that wraps
// every screen in NoteHub.
//
// It contains two composable functions:
//  1. MainScreen()  → Builds the Scaffold (Top Bar + Bottom Nav)
//  2. AppNavHost()  → Defines all screen routes and their callbacks
//
// Think of MainScreen as the "frame" of a picture — it stays fixed
// while the "picture" (the screen content) changes inside it.


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController          // controller object that handles navigation actions
import androidx.navigation.compose.NavHost           // the container that holds all screen routes
import androidx.navigation.compose.composable        // registers a screen at a specific route
import androidx.navigation.compose.currentBackStackEntryAsState // observes the current screen as state
import androidx.navigation.compose.rememberNavController        // creates and remembers the nav controller
import com.example.notehub.navigation.Screen        // our sealed class with all screen routes
import com.example.notehub.screens.*                // imports all screen composables (Login, Dashboard, etc.)
import com.example.notehub.ui.theme.*               // imports all theme colors (PrimaryBlue, TextSecondary, etc.)
import androidx.compose.ui.tooling.preview.Preview

// @OptIn tells the compiler we are aware we are using an experimental API (TopAppBar)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {

    // navController manages all navigation — going forward, going back, clearing stack
    val navController = rememberNavController()

    // Observes the current back stack entry as a Compose state
    // When the user navigates, this automatically updates → triggers recomposition
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // Extract the route string of the current screen (e.g. "dashboard", "login")
    val currentRoute = navBackStackEntry?.destination?.route

    // Check if the top bar and bottom nav should be shown on this screen
    // Returns FALSE for Login, SignUp, and AddNote (they have their own UI)
    // Returns TRUE for Dashboard, Notes, Uploads, Settings
    val shouldShowNavigation = Screen.shouldShowDrawer(currentRoute)

    // Scaffold is the standard Material Design layout structure
    // It positions the topBar, bottomBar, and main content automatically
    Scaffold(

        // ── TOP APP BAR ────────────────────────────────────────────
        topBar = {
            // Only show the top bar when shouldShowNavigation is true
            if (shouldShowNavigation) {
                TopAppBar(
                    title = {
                        Text(
                            // Find the current screen's title from drawerScreens list
                            // If not found (e.g. unknown route), fall back to "NoteHub"
                            // ?: is the Elvis operator — returns right side if left side is null
                            text = Screen.drawerScreens.find { it.route == currentRoute }?.title ?: "NoteHub",
                            fontWeight = FontWeight.SemiBold // bold-ish title text
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,       // bar background color
                        titleContentColor = MaterialTheme.colorScheme.onSurface   // title text color
                    )
                )
            }
        },

        // ── BOTTOM NAVIGATION BAR ──────────────────────────────────
        bottomBar = {
            // Only show the bottom nav when shouldShowNavigation is true
            if (shouldShowNavigation) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface, // bar background color
                    contentColor = PrimaryBlue                          // default icon tint
                ) {
                    // Loop through the 4 main screens: Dashboard, Notes, Uploads, Settings
                    Screen.drawerScreens.forEach { screen ->
                        NavigationBarItem(
                            // Icon shown in the bottom tab
                            icon = {
                                screen.icon?.let { icon -> // ?.let safely handles null icon
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title // for accessibility
                                    )
                                }
                            },
                            label = { Text(screen.title) }, // text label below the icon

                            // This tab is "selected" (highlighted) when we are on its screen
                            selected = currentRoute == screen.route,

                            // Called when the user taps a bottom nav tab
                            onClick = {
                                navController.navigate(screen.route) {
                                    // Pop back to Dashboard to avoid a huge back stack
                                    // saveState = true remembers the state of that screen
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = true
                                }
                            },

                            // Color settings for selected vs unselected tabs
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,              // blue when active
                                selectedTextColor = PrimaryBlue,              // blue text when active
                                indicatorColor = PrimaryBlue.copy(alpha = 0.1f), // very light blue circle behind active icon
                                unselectedIconColor = TextSecondary,          // grey when inactive
                                unselectedTextColor = TextSecondary           // grey text when inactive
                            )
                        )
                    }
                }
            }
        },

        containerColor = MaterialTheme.colorScheme.background // app background color
    ) { paddingValues ->
        // paddingValues prevents content from being hidden behind the top/bottom bars
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues) // apply safe padding from Scaffold
        )
    }
}


// AppNavHost — Defines all the screen routes and their callbacks.
//
// NavHost is like a "router" — when navController says
// "go to dashboard", NavHost shows DashboardScreen().
//
// @param navController  The controller that manages screen navigation
// @param modifier       Applies the padding from Scaffold

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route, // first screen shown when app launches
        modifier = modifier
    ) {

        // ── LOGIN SCREEN ───────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Navigate to Dashboard and remove Login from back stack
                    // inclusive = true means Login itself is also removed
                    // So user can't press Back to return to Login after signing in
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route) // push SignUp onto stack
                }
            )
        }

        // ── SIGN UP SCREEN ─────────────────────────────────────────
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    // After sign up, go to Dashboard and clear Login + SignUp from stack
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack() // go back one step → returns to Login
                }
            )
        }

        // ── DASHBOARD SCREEN ───────────────────────────────────────
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddNote = {
                    navController.navigate(Screen.AddNote.route)
                },
                onNavigateToUploads = {
                    navController.navigate(Screen.Uploads.route) {
                        popUpTo(Screen.Dashboard.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }

        // ── NOTES SCREEN ───────────────────────────────────────────
        composable(Screen.Notes.route) {
            NotesScreen(
                onAddNoteClick = {
                    navController.navigate(Screen.AddNote.route) // open AddNote screen
                },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId") // open NoteDetail screen
                }
            )
        }

        // ── NOTE DETAIL SCREEN ──────────────────────────────────────
        composable(
            route = Screen.NoteDetail.route
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── ADD NOTE SCREEN ────────────────────────────────────────
        composable(Screen.AddNote.route) {
            AddNoteScreen(
                onNavigateBack = {
                    navController.popBackStack() // go back to Notes screen
                }
            )
        }

        // ── UPLOADS SCREEN ─────────────────────────────────────────
        composable(Screen.Uploads.route) {
            UploadsScreen() // no callbacks needed
        }

        // ── SETTINGS SCREEN ────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    // Navigate to Login and WIPE the entire back stack (popUpTo(0))
                    // This is a SECURITY FEATURE — user can't press Back after logout
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)           // 0 = clears ALL screens from the back stack
                        launchSingleTop = true // prevent duplicate Login screen
                    }
                }
            )
        }
    }
}

// ── PREVIEW ───────────────────────────────────────────────────
// Used only by Android Studio's Design Preview — does NOT run on device
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    NoteHubTheme {
        MainScreen()
    }
}
