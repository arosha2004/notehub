# 📄 MainScreen.kt — Explanation

> This is the **app shell** — it holds the top bar, bottom navigation bar, and controls which screen is currently shown.

---

## 🧠 What Does This File Do?

Think of `MainScreen` like a **shopping mall**:
- The **top bar** = the mall's name sign at the entrance
- The **bottom bar** = the map at the bottom showing which section you're in
- The **content area** = the actual store you're visiting right now

---

## 🔍 Section-by-Section Explanation

---

### 📦 Package & Imports

```kotlin
package com.example.notehub
```
This file lives in the root `notehub` package.

The imports bring in tools for:
- `layout.*` — Column, padding, size tools
- `material3.*` — Material Design UI components (TopAppBar, NavigationBar, etc.)
- `navigation.*` — Tools to control screen switching (NavController, NavHost, etc.)
- `Screen` — Our list of app routes
- `screens.*` — All the screen composables
- `theme.*` — Our colors

---

### 🏗️ MainScreen Function

```kotlin
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val shouldShowNavigation = Screen.shouldShowDrawer(currentRoute)
```

| Code | What it does |
|---|---|
| `rememberNavController()` | Creates the **navigation GPS** of the app — controls which screen shows |
| `currentBackStackEntryAsState()` | Watches which screen is currently active |
| `currentRoute` | Gets the name (route string) of the current screen, like `"dashboard"` |
| `shouldShowNavigation` | A true/false: should we show the top & bottom bars right now? |

> 🧠 The bars are **hidden** on: Login, Sign Up, and Add Note screens. They only show on main app screens.

---

### 🔝 Top App Bar

```kotlin
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
}
```

| Code | What it does |
|---|---|
| `if (shouldShowNavigation)` | Only show the top bar on main screens |
| `TopAppBar(...)` | The bar at the very top of the screen |
| `Screen.drawerScreens.find { it.route == currentRoute }` | Searches the list of screens to find the current one |
| `?.title ?: "NoteHub"` | Gets that screen's title. If not found, defaults to "NoteHub" |
| `containerColor = ...surface` | Top bar background matches the theme surface color |

---

### ⬇️ Bottom Navigation Bar

```kotlin
bottomBar = {
    if (shouldShowNavigation) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = PrimaryBlue
        ) {
            Screen.drawerScreens.forEach { screen ->
                NavigationBarItem(
                    icon = { Icon(imageVector = screen.icon!!, contentDescription = screen.title) },
                    label = { Text(screen.title) },
                    selected = currentRoute == screen.route,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
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
}
```

| Code | What it does |
|---|---|
| `NavigationBar` | The bar at the very bottom of the screen with icons |
| `Screen.drawerScreens.forEach { screen -> ... }` | Loops through Dashboard, Notes, Uploads, Settings and creates a tab for each |
| `icon = { Icon(...) }` | Shows the icon for each tab |
| `label = { Text(screen.title) }` | Shows the text label below each icon |
| `selected = currentRoute == screen.route` | Highlights the tab if it's the currently active screen |
| `navController.navigate(screen.route) { ... }` | Takes the user to the tapped screen |
| `popUpTo(Screen.Dashboard.route)` | Prevents building up a huge back-stack (like 20 screens in history) |
| `launchSingleTop = true` | Don't open a second copy of the same screen |
| `restoreState = true` | Remember the scroll position etc. when coming back |
| `selectedIconColor = PrimaryBlue` | Active tab icon is blue |
| `unselectedIconColor = TextSecondary` | Inactive tab icons are grey |

---

### 🧭 AppNavHost — The Navigation Map

```kotlin
@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) }
            )
        }
        composable(Screen.SignUp.route) { ... }
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.Notes.route) { NotesScreen(onAddNoteClick = { navController.navigate(Screen.AddNote.route) }) }
        composable(Screen.AddNote.route) { AddNoteScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Uploads.route) { UploadsScreen() }
        composable(Screen.Settings.route) { SettingsScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
    }
}
```

| Code | What it does |
|---|---|
| `NavHost(...)` | The container that manages all screen routes |
| `startDestination = Screen.Login.route` | The first screen shown when the app opens is Login |
| `composable(Screen.Login.route) { ... }` | Says "when route is 'login', show LoginScreen" |
| `popUpTo(Screen.Login.route) { inclusive = true }` | After logging in, remove the Login screen from back history so the user can't press Back to return to it |
| `navController.popBackStack()` | Go back to the previous screen |
| `popUpTo(0)` | On logout, clear ALL screens and go all the way back to the very first screen |

---

## 🗺️ Navigation Flow Diagram

```
LOGIN ──────────────────────────────────────────────┐
  │ onLoginSuccess                                   │ (back stack cleared)
  ▼                                                  │
DASHBOARD ◄──── Bottom Nav ────► NOTES              │
  │                                │                 │
  │                                │ onAddNoteClick  │
  │                         ADD NOTE SCREEN          │
  │                                │ onNavigateBack  │
  │                                ▼                 │
  │                             NOTES               │
  │                                                  │
  └──── Bottom Nav ──► UPLOADS / SETTINGS            │
                            │ onLogout               │
                            └───────────────────────►┘
```

---

## ⭐ Key Takeaway

> `MainScreen.kt` is the **app skeleton**. It doesn't show content itself — it:
> 1. Controls the **top bar** title
> 2. Controls the **bottom navigation bar** tabs
> 3. Maps every route to the correct screen via `AppNavHost`
