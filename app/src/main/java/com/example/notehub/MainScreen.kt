package com.example.notehub

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
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
    val sharedViewModel: LocationNotesViewModel = viewModel()

    Scaffold(
        topBar = {
            if (shouldShowNavigation) {
                TopAppBar(
                    title = {
                        Text(
                            text = Screen.drawerScreens.find { it.route == currentRoute }?.title ?: "Location Notes",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        val isPrimaryScreen = Screen.drawerScreens.any { it.route == currentRoute }
                        if (!isPrimaryScreen && currentRoute != null) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
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
            sharedViewModel = sharedViewModel,
            modifier = Modifier.padding(paddingValues)
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    sharedViewModel: LocationNotesViewModel,
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
                    sharedViewModel.fetchNotes()
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
                    sharedViewModel.fetchNotes()
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
                onNavigateToLocationNotes = { navController.navigate(Screen.LocationNotes.route) },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.Notes.route) {
            NotesScreen(
                onAddNoteClick = {
                    navController.navigate(Screen.AddNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.NoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { id ->
                    navController.navigate("edit_note/$id")
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.AddNote.route) {
            AddNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.EditNote.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull()
            AddNoteScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.LocationNotes.route) {
            LocationNotesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAddNoteClick = {
                    navController.navigate(Screen.AddLocationNote.route)
                },
                onNoteClick = { noteId ->
                    navController.navigate("location_note_detail/$noteId")
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.AddLocationNote.route) {
            AddLocationNoteScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.LocationNoteDetail.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toIntOrNull() ?: 0
            LocationNoteDetailScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEditClick = { id ->
                    navController.navigate("edit_note/$id")
                },
                viewModel = sharedViewModel
            )
        }

        composable(Screen.Uploads.route) {
            UploadsScreen(
                viewModel = sharedViewModel
            )
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
