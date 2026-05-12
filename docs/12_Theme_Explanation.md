# 📄 Theme.kt — Explanation

> This file **assembles the color schemes** and applies them to the whole app through `NoteHubTheme`.

---

## 🧠 What Is This File For?

Think of `Theme.kt` as the **master light switch panel** of the app:
- It takes all the colors from `Color.kt`
- Groups them into complete **color schemes** (Light Indigo, Dark Indigo, Light Ocean, Dark Ocean)
- Exposes a single `NoteHubTheme()` function that wraps the entire app

---

## 🎨 What Is a Color Scheme?

A `colorScheme` is like a **costume** for the app. Material 3 defines named slots:
- `primary` — the main brand color (buttons, active icons)
- `background` — the screen background
- `surface` — card/panel backgrounds
- `onBackground` — text color on backgrounds
- `onSurface` — text color on surfaces
- `error` — color for error states
- etc.

By filling these slots with different colors, you change the entire look of the app.

---

## 🔍 Section-by-Section Explanation

---

### Light Indigo Color Scheme

```kotlin
private val IndigoLightColorScheme = lightColorScheme(
    primary = PrimaryBlue,           // Buttons, active tabs = Indigo
    onPrimary = TextOnPrimary,       // Text on buttons = White
    background = LightBlueGrey,      // Screen background = Light purple-white
    onBackground = TextPrimary,      // Text on background = Deep purple-black
    surface = BackgroundWhite,       // Cards = Pure white
    onSurface = TextPrimary,         // Text on cards = Deep purple-black
    surfaceVariant = SurfaceWhite,   // Input variants = Almost white
    onSurfaceVariant = TextSecondary,// Text in inputs = Cool grey
    error = ErrorRed,                // Error states = Red
    outline = BorderMedium,          // Borders = Grey
    ...
)
```

This is the **default theme** — the one you see when you first open the app.

---

### Dark Indigo Color Scheme

```kotlin
private val IndigoDarkColorScheme = darkColorScheme(
    primary = DarkPrimaryBlue,       // Lighter indigo (easier on eyes in dark)
    background = DarkBackground,     // Deep purple-black background
    surface = DarkSurface,           // Dark purple cards
    onBackground = DarkTextPrimary,  // Almost-white text
    onSurface = DarkTextPrimary,     // Almost-white text on cards
    ...
)
```

This activates when **Dark Mode toggle is ON** in Settings.

---

### Ocean Light & Dark Color Schemes

```kotlin
private val OceanLightColorScheme = lightColorScheme(
    primary = OceanPrimary,          // Cyan instead of indigo
    background = OceanLightBackground,
    ...
)

private val OceanDarkColorScheme = darkColorScheme(
    primary = OceanDarkPrimary,      // Bright cyan for dark mode
    background = OceanDarkBackground,
    ...
)
```

These activate when `ThemeManager.currentTheme == AppTheme.OCEAN`. The look changes from indigo/purple to cyan/blue.

---

### The NoteHubTheme Function

```kotlin
@Composable
fun NoteHubTheme(
    darkTheme: Boolean = ThemeManager.isDarkMode,
    currentTheme: AppTheme = ThemeManager.currentTheme,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        currentTheme == AppTheme.OCEAN -> {
            if (darkTheme) OceanDarkColorScheme else OceanLightColorScheme
        }
        else -> {
            if (darkTheme) IndigoDarkColorScheme else IndigoLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

Breaking this down step by step:

#### Parameters

| Parameter | Default | What it means |
|---|---|---|
| `darkTheme` | `ThemeManager.isDarkMode` | Is dark mode currently on? |
| `currentTheme` | `ThemeManager.currentTheme` | Which theme? Indigo or Ocean? |
| `dynamicColor` | `false` | Should Android auto-pick colors from the wallpaper? (disabled) |
| `content` | — | The UI to wrap with this theme |

#### The `when` Decision Tree

```
when {
    dynamicColor is true AND Android 12+  →  Use phone's wallpaper colors (disabled)
    currentTheme is OCEAN                 →  Use Ocean (dark or light)
    else (default, INDIGO)               →  Use Indigo (dark or light)
}
```

So the actual flowchart is:
```
Is dynamic color on? No
       ↓
Is theme OCEAN?
  Yes → Dark mode on? → OceanDark : OceanLight
  No  → Dark mode on? → IndigoDark : IndigoLight (← default)
```

#### MaterialTheme

```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
)
```

- `MaterialTheme` — This is the official Material 3 theme wrapper from Google
- `colorScheme` — The color scheme we selected above
- `typography = Typography` — The font definitions from `Type.kt`
- `content` — Everything inside `NoteHubTheme { ... }` gets this theme applied

---

## 🔄 How the Theme Updates Happen

1. User taps the **Dark Mode toggle** in Settings
2. `ThemeManager.setDarkMode(true)` is called
3. `ThemeManager.isDarkMode` changes to `true`
4. Since `NoteHubTheme` reads `ThemeManager.isDarkMode`, Compose **automatically re-reads it**
5. `colorScheme` is recalculated → `IndigoDarkColorScheme`
6. The whole app **re-draws** with dark colors

This is the power of Compose's reactive state system — it all happens automatically!

---

## ⭐ Key Takeaway

> `Theme.kt` is the **color scheme assembler**. It:
> 1. Defines 4 complete color schemes: Indigo Light, Indigo Dark, Ocean Light, Ocean Dark
> 2. Provides `NoteHubTheme()` which **picks the right scheme** based on `ThemeManager` state
> 3. Wraps everything with `MaterialTheme` so all components automatically use the right colors
>
> When you use `MaterialTheme.colorScheme.primary` anywhere in a screen, it returns the right color automatically — no manual work needed!
