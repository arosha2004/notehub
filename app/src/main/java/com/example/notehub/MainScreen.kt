package com.example.notehub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.notehub.navigation.Screen
import com.example.notehub.screens.*
import com.example.notehub.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowNavigation = Screen.shouldShowDrawer(currentRoute)

    Scaffold(
        topBar = {
            if (shouldShowNavigation) {
                TopAppBar(
                    title = {
                        Text(
                            text = Screen.drawerScreens.find { it.route == currentRoute }?.title ?: "NoteHub",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        bottomBar = {
            if (shouldShowNavigation) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryBlue
                ) {
                    Screen.drawerScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = PrimaryBlue.copy(alpha = 0.1f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddNote = { navController.navigate(Screen.AddNote.route) },
                onNavigateToUploads = { navController.navigate(Screen.Uploads.route) },
                onNavigateToLocationNotes = { navController.navigate(Screen.LocationNotes.route) }
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onAddNoteClick = {
                    navController.navigate(Screen.AddNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                }
            )
        }

        composable(Screen.NoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AddNote.route) {
            AddNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LocationNotes.route) {
            LocationNotesScreen(
                onAddNoteClick = {
                    navController.navigate(Screen.AddLocationNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate("location_note_detail/$noteId")
                }
            )
        }

        composable(Screen.AddLocationNote.route) {
            AddLocationNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.LocationNoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
            LocationNoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Uploads.route) {
            UploadsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
