# 📄 Screen.kt — Explanation

> This file is the **route map** of the app. It defines a name and path for every screen.

---

## 📋 Full File Content

```kotlin
package com.example.notehub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    // Auth Screens (no icon needed)
    data object Login : Screen("login", "Login")
    data object SignUp : Screen("signup", "Sign Up")

    // Main App Screens (with navigation drawer items)
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
    data object Notes : Screen("notes", "All Notes", Icons.Filled.Note)
    data object Uploads : Screen("uploads", "Uploads", Icons.Filled.Upload)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)

    // Feature Screens (no icon needed for navigation)
    data object AddNote : Screen("add_note", "Add Note")

    companion object {
        val drawerScreens = listOf(Dashboard, Notes, Uploads, Settings)

        fun shouldShowDrawer(route: String?): Boolean {
            return route != Login.route && route != SignUp.route && route != AddNote.route
        }
    }
}
```

---

## 🔍 Line-by-Line Explanation

---

### Line 1 — Package
```kotlin
package com.example.notehub.navigation
```
This file is in the `navigation` sub-folder. It handles navigation (screen routing) logic.

---

### Lines 3–8 — Imports

| Import | What it gives us |
|---|---|
| `Icons` | The built-in icon collection |
| `Icons.Filled.Dashboard` | The house/dashboard icon |
| `Icons.Filled.Note` | The notepad icon |
| `Icons.Filled.Settings` | The gear settings icon |
| `Icons.Filled.Upload` | The upload arrow icon |
| `ImageVector` | The data type for icons |

---

### Line 10 — The Sealed Class

```kotlin
sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
```

- `sealed class` — A special class where you list ALL possible options inside it. Like a menu — you can only pick from what's listed.
- `Screen(val route, val title, val icon)` — Every screen has 3 pieces of info:
  - `route` — The text ID used to navigate (like a web URL: `"dashboard"`)
  - `title` — The human-readable name shown in the top bar
  - `icon` — The icon shown in the bottom navigation bar (optional, `? = null`)

> 🧠 **Analogy:** A `sealed class` is like a locked menu at a restaurant — the chef (developer) decides what options exist. Customers (code) can only choose from that list.

---

### Lines 11–13 — Auth Screens (No Icons)

```kotlin
data object Login : Screen("login", "Login")
data object SignUp : Screen("signup", "Sign Up")
```

- `data object` — Creates a single, unique instance of a screen definition
- `Login` — Route is `"login"`, title is `"Login"`, no icon (these screens don't appear in the bottom nav bar)
- `SignUp` — Route is `"signup"`, title is `"Sign Up"`, no icon

---

### Lines 15–19 — Main App Screens (With Icons)

```kotlin
data object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Dashboard)
data object Notes : Screen("notes", "All Notes", Icons.Filled.Note)
data object Uploads : Screen("uploads", "Uploads", Icons.Filled.Upload)
data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
```

These 4 screens appear in the **bottom navigation bar** because they have icons.

| Screen | Route | Title | Icon |
|---|---|---|---|
| Dashboard | `"dashboard"` | Dashboard | 🏠 |
| Notes | `"notes"` | All Notes | 📝 |
| Uploads | `"uploads"` | Uploads | ⬆️ |
| Settings | `"settings"` | Settings | ⚙️ |

---

### Line 21–22 — Feature Screen (No Icon)

```kotlin
data object AddNote : Screen("add_note", "Add Note")
```

`AddNote` has no icon because it's not a tab in the bottom bar — it's a temporary screen you go to and come back from.

---

### Lines 24–30 — Companion Object

```kotlin
companion object {
    val drawerScreens = listOf(Dashboard, Notes, Uploads, Settings)

    fun shouldShowDrawer(route: String?): Boolean {
        return route != Login.route && route != SignUp.route && route != AddNote.route
    }
}
```

- `companion object` — Code that belongs to the class itself, not to any specific instance. Like a utility toolbox attached to the class.
- `drawerScreens` — A fixed list of the 4 screens that appear in the bottom nav bar
- `shouldShowDrawer(route)` — A function that returns `true` if we should show the top/bottom bars
  - Returns `false` (hide bars) for: Login, SignUp, AddNote
  - Returns `true` (show bars) for: Dashboard, Notes, Uploads, Settings

> 🧠 **Why hide the bars on Login/SignUp/AddNote?** Because those screens have their own UI controls (Back button, Save button) and don't need the main navigation.

---

## 🗺️ Route Table

| Constant | Route String | Has Bottom Tab? |
|---|---|---|
| `Screen.Login` | `"login"` | ❌ No |
| `Screen.SignUp` | `"signup"` | ❌ No |
| `Screen.Dashboard` | `"dashboard"` | ✅ Yes |
| `Screen.Notes` | `"notes"` | ✅ Yes |
| `Screen.Uploads` | `"uploads"` | ✅ Yes |
| `Screen.Settings` | `"settings"` | ✅ Yes |
| `Screen.AddNote` | `"add_note"` | ❌ No |

---

## ⭐ Key Takeaway

> `Screen.kt` is the **master list of all screens and their routes**. Instead of typing `"dashboard"` as a raw text string everywhere (which could cause typos), we use `Screen.Dashboard.route` — safe, reusable, and consistent.
