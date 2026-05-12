# 📄 ThemeManager.kt — Explanation

> This is the **global theme controller** — it holds the current dark mode and theme state for the entire app.

---

## 📋 Full File Content

```kotlin
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
```

---

## 🔍 Line-by-Line Explanation

---

### Imports

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
```

These are needed because `ThemeManager` uses `mutableStateOf` — Compose's reactive state system — but **outside** of a `@Composable` function. These imports make it work in a regular `object`.

---

### The `object` Keyword

```kotlin
object ThemeManager {
```

- `object` creates a **singleton** — there is only ONE `ThemeManager` in the entire app
- You don't create it with `ThemeManager()` — it already exists
- You just access it: `ThemeManager.isDarkMode`, `ThemeManager.setDarkMode(true)`

> 🧠 **Analogy:** `object` is like the ONE thermostat in a building — there's only one, and everyone reads from and writes to the same one.

---

### Current Theme State

```kotlin
private var _currentTheme by mutableStateOf(AppTheme.INDIGO)
val currentTheme: AppTheme
    get() = _currentTheme
```

- `private var _currentTheme` — The actual stored value (private: only ThemeManager can change it directly)
- `mutableStateOf(AppTheme.INDIGO)` — Starts as INDIGO theme. Using `mutableStateOf` means when it changes, Compose **automatically redraws** any screen reading it
- `val currentTheme: AppTheme` — A public read-only property anyone can read
- `get() = _currentTheme` — Reading `currentTheme` returns the private `_currentTheme` value

> 🧠 This pattern (private `_variable` + public `variable`) is called **encapsulation** — only ThemeManager controls the changes, but anyone can read the value.

---

### Dark Mode State

```kotlin
private var _isDarkMode by mutableStateOf(false)
val isDarkMode: Boolean
    get() = _isDarkMode
```

- `_isDarkMode` — The actual stored dark mode state (private)
- Starts as `false` (light mode by default)
- `isDarkMode` — Public read-only version
- Because it uses `mutableStateOf`, any screen reading `ThemeManager.isDarkMode` **automatically redraws** when it changes

---

### setTheme() Function

```kotlin
fun setTheme(theme: AppTheme) {
    _currentTheme = theme
}
```

- Changes the theme to INDIGO or OCEAN
- Example usage: `ThemeManager.setTheme(AppTheme.OCEAN)`
- Currently not called from anywhere in the UI (would need a theme selector)

---

### toggleDarkMode() Function

```kotlin
fun toggleDarkMode() {
    _isDarkMode = !_isDarkMode
}
```

- `!_isDarkMode` means "the opposite of the current value"
- If dark mode is ON (`true`), this turns it OFF (`false`) — and vice versa
- Example: `ThemeManager.toggleDarkMode()` (currently not used — `setDarkMode` is used instead)

---

### setDarkMode() Function

```kotlin
fun setDarkMode(enabled: Boolean) {
    _isDarkMode = enabled
}
```

- Sets dark mode to a specific value: `true` (on) or `false` (off)
- **This is what SettingsScreen calls** when the Dark Mode toggle is switched:
  ```kotlin
  // In SettingsScreen.kt
  onCheckedChange = { ThemeManager.setDarkMode(it) }
  ```
- `it` is Kotlin shorthand for the parameter passed to the lambda — in this case the new toggle value (true or false)

---

### AppTheme Enum

```kotlin
enum class AppTheme {
    INDIGO,  // Purple/Indigo theme (default)
    OCEAN    // Blue/Teal theme
}
```

- `enum class` — A type that can only be one of a fixed set of values
- `AppTheme.INDIGO` — The indigo/purple theme (default)
- `AppTheme.OCEAN` — The cyan/blue ocean theme (alternative)

> 🧠 **Analogy:** An `enum` is like a traffic light — it can only be RED, YELLOW, or GREEN. `AppTheme` can only be `INDIGO` or `OCEAN`.

---

## 🔄 How It All Works Together

```
User taps Dark Mode toggle in SettingsScreen
              ↓
ThemeManager.setDarkMode(true) is called
              ↓
_isDarkMode changes from false → true
              ↓
Because it uses mutableStateOf, Compose detects the change
              ↓
Theme.kt's NoteHubTheme reads ThemeManager.isDarkMode = true
              ↓
Picks IndigoDarkColorScheme
              ↓
ENTIRE APP redraws with dark colors 🌙
```

---

## ⭐ Key Takeaway

> `ThemeManager.kt` is the **global switch** for the app's visual theme. It:
> 1. Holds the current theme (Indigo or Ocean)
> 2. Holds dark mode on/off
> 3. Uses `mutableStateOf` so changes **automatically update the UI**
> 4. Is a **singleton** — one instance shared by the whole app
>
> The Dark Mode toggle in Settings calls `ThemeManager.setDarkMode()` → everything updates automatically!
