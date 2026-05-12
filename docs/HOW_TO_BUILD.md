# 🏗️ How to Build NoteHub — Step-by-Step Guide

This guide explains the chronological steps followed to build the **NoteHub** Android application using **Kotlin** and **Jetpack Compose**.

---

## 🛠️ Phase 1: The UI Foundation (Design System)
Before building any screens, we first defined the "look and feel" of the app. This is the **UI First** approach.

### Step 1: Define the Colors (`Color.kt`)
We created a central file to store all our hex codes. This ensures consistency across the whole app.
```kotlin
// Example from Color.kt
val PrimaryBlue = Color(0xFF6366F1) // The main brand color
val LightBlueGrey = Color(0xFFF8F7FF) // Background tint
val TextPrimary = Color(0xFF0F0A1E) // High contrast text
```

### Step 2: Create a Theme Manager (`ThemeManager.kt`)
We created a **Singleton** object to manage the system theme (Dark/Light mode) globally.
```kotlin
object ThemeManager {
    var isDarkMode by mutableStateOf(false)
    fun toggleDarkMode() { isDarkMode = !isDarkMode }
}
```

### Step 3: Set up the Material Theme (`Theme.kt`)
We mapped our colors to Material 3 roles (Primary, Surface, Background) for both Light and Dark modes.

---

## 🧭 Phase 2: Navigation & Structure

### Step 4: Define Screen Routes (`Screen.kt`)
We used a **Sealed Class** to define every screen in our app so we don't make typos in navigation.
```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object dashboard : Screen("dashboard")
    object Notes : Screen("notes")
}
```

### Step 5: Build the App Shell (`MainScreen.kt`)
We created a `Scaffold` that contains the **Bottom Navigation Bar** and a `NavHost` to swap screens.
```kotlin
Scaffold(
    bottomBar = { /* Bottom Navigation Code here */ }
) {
    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) { LoginScreen(...) }
        // ... other screens
    }
}
```

---

## 📱 Phase 3: Building the Screens (UI Implementation)

We built the screens in this order:

### Step 6: Login & Sign Up Screens
Focus was on **Input Validation** and **UI Layout**.
*   Used `OutlinedTextField` for inputs.
*   Used `remember` state for text input.
*   Added logic to enable the "Sign In" button only if fields are not empty.

### Step 7: The Dashboard Screen
The "Home" of the app.
*   Created a **Welcome Card** with a gradient background.
*   Built a **Stats Grid** (2x2) using `Row` and `Column`.
*   Added **Quick Action** buttons for fast access.

### Step 8: The Notes Screen
*   Created a `Note` **Data Class** to hold note information.
*   Used `LazyColumn` to show a list of notes efficiently.
*   Designed a `NoteCard` for each item.

### Step 9: Settings & Dark Mode Logic
*   Added a `Switch` for Dark Mode.
*   Linked it to `ThemeManager.isDarkMode`.
*   Added a **Log Out** button that clears the navigation stack.

---

## 🏁 Phase 4: Final Integration

### Step 10: The Entry Point (`MainActivity.kt`)
Finally, we set the theme and called the `MainScreen`.
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NoteHubTheme { // Applies our colors & dark mode
                MainScreen() // Launches the app shell
            }
        }
    }
}
```

---

## 💡 Summary of Technology Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Modern Android UI)
*   **Navigation**: Jetpack Navigation Component
*   **State**: `mutableStateOf` and `remember`
*   **Theme**: Material Design 3 (M3)
