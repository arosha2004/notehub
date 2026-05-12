# 📄 Color.kt — Explanation

> This file defines **every color used in the entire NoteHub app**, for both Light Mode and Dark Mode.

---

## 🧠 What Is This File For?

Instead of writing raw colors like `#6366F1` scattered all over the code, we define them ONCE here with meaningful names like `PrimaryBlue`. Then every other file uses the name — this makes it easy to change a color in one place and have it update everywhere.

> 🧠 **Analogy:** This is like a brand color guide. If NoteHub's brand color changes, you only update this one file.

---

## 🎨 How Colors Are Written

```kotlin
val PrimaryBlue = Color(0xFF6366F1)
```

- `val` — This value never changes (fixed color)
- `PrimaryBlue` — The name we give this color
- `Color(0xFF6366F1)` — The actual color in hexadecimal format

**Understanding the hex color format:**
```
Color(0xFF6366F1)
      ↑↑ ↑↑ ↑↑ ↑↑
      FF 63 66 F1
      Alpha Red Green Blue
```
- `FF` = Alpha (opacity) — FF means fully opaque (100% visible)
- `63` = Red amount
- `66` = Green amount
- `F1` = Blue amount

> `0xFF` at the start simply means the number is in hexadecimal (base-16). You don't need to memorize this — just know that changing the last 6 characters changes the color.

---

## 🌞 Light Mode Colors

### Primary Colors (Main Blue/Indigo)

```kotlin
val PrimaryBlue = Color(0xFF6366F1)       // Vibrant Indigo — used for buttons, active tabs, borders
val PrimaryBlueLight = Color(0xFF818CF8)  // Lighter version
val PrimaryBlueDark = Color(0xFF4F46E5)   // Darker version
```

These are the main brand colors — the indigo/purple-blue that appears on buttons, active icons, etc.

---

### Background Colors

```kotlin
val LightBlueGrey = Color(0xFFF8F7FF)   // Very light purple-tinted white — main screen background
val BackgroundWhite = Color(0xFFFFFFFF) // Pure white — used for cards and surfaces
val SurfaceWhite = Color(0xFFFDFDFE)    // Almost white with slight purple — used for input fields
```

- The main background is not pure white — it has a very subtle purple tint (`#F8F7FF`) for a premium feel

---

### Text Colors

```kotlin
val TextPrimary = Color(0xFF0F0A1E)     // Deep purple-black — for headings and important text
val TextSecondary = Color(0xFF6B7280)   // Cool grey — for labels and secondary text
val TextTertiary = Color(0xFF9CA3AF)    // Light grey — for dates and muted text
val TextOnPrimary = Color(0xFFFFFFFF)   // Pure white — text on colored buttons/backgrounds
val TextOnDark = Color(0xFFE5E7EB)      // Light grey — text on dark/gradient backgrounds
```

| Color | Used For |
|---|---|
| `TextPrimary` | Titles and main content |
| `TextSecondary` | Labels, placeholders |
| `TextTertiary` | Dates, subtle info |
| `TextOnPrimary` | Text/icons on blue buttons |
| `TextOnDark` | Text on gradient banners |

---

### Status Colors

```kotlin
val SuccessGreen = Color(0xFF10B981)  // Emerald green — for success states
val ErrorRed = Color(0xFFEF4444)      // Coral red — for errors and logout button
val WarningYellow = Color(0xFFFBBF24) // Amber — for warnings
val InfoBlue = Color(0xFF3B82F6)      // Bright blue — for informational elements
```

These are called **semantic colors** — each has a specific meaning:
- 🟢 Green = Success / Good
- 🔴 Red = Error / Danger / Logout
- 🟡 Yellow = Warning / Caution
- 🔵 Blue = Info / Neutral

---

### Accent Colors (Note Colors)

```kotlin
val AccentGold = Color(0xFFFBBF24)    // Gold
val AccentPurple = Color(0xFF8B5CF6)  // Purple
val AccentRose = Color(0xFFEC4899)    // Rose/Pink
```

These are the extra colors available for notes (alongside the status colors) in the AddNoteScreen color picker.

---

### Border Colors

```kotlin
val BorderLight = Color(0xFFE5E7EB)   // Very light grey — for subtle input field borders
val BorderMedium = Color(0xFFD1D5DB)  // Slightly darker grey — for more visible borders
val Divider = Color(0xFFE5E7EB)       // Same as BorderLight — for divider lines
```

---

### Gradient Colors

```kotlin
val GradientStart = Color(0xFF6366F1) // Indigo — gradient starting color
val GradientEnd = Color(0xFF8B5CF6)   // Purple — gradient ending color
```

These are used in:
- The logo box background (Login & SignUp screens)
- The WelcomeCard banner on Dashboard
- Creates a beautiful **indigo → purple** gradient

---

## 🌙 Dark Mode Colors

When dark mode is ON, these colors replace the light mode ones:

### Dark Backgrounds

```kotlin
val DarkBackground = Color(0xFF0A0512)     // Deep Purple Black — main screen background
val DarkSurface = Color(0xFF1E1B2E)        // Dark Purple — card/surface background
val DarkSurfaceVariant = Color(0xFF2D2A3D) // Slightly lighter — for variant surfaces
val DarkCardBackground = Color(0xFF1E1B2E) // Same as DarkSurface for cards
```

Dark mode backgrounds are very dark purple-black tones.

### Dark Text Colors

```kotlin
val DarkTextPrimary = Color(0xFFF9FAFB)    // Almost white — main text in dark mode
val DarkTextSecondary = Color(0xFFD1D5DB)  // Light grey — secondary text
val DarkTextTertiary = Color(0xFF9CA3AF)   // Medium grey — muted text
```

In dark mode, text becomes light so it's readable on dark backgrounds.

### Dark Borders & Icons

```kotlin
val DarkBorderLight = Color(0xFF374151)   // Dark grey borders
val DarkBorderMedium = Color(0xFF4B5563)  // Slightly lighter dark grey
val DarkIconPrimary = Color(0xFFD1D5DB)   // Light grey icons
```

---

## 🌊 Ocean Blue Theme Colors

The app also has an alternative **Ocean Blue** theme:

```kotlin
val OceanPrimary = Color(0xFF0891B2)         // Cyan — replaces Indigo as primary
val OceanGradientStart = Color(0xFF0891B2)   // Cyan gradient start
val OceanGradientEnd = Color(0xFF3B82F6)     // Blue gradient end
val OceanDarkBackground = Color(0xFF0A1628)  // Deep Blue Black for dark ocean theme
```

When `ThemeManager.currentTheme == AppTheme.OCEAN`, these replace the indigo colors.

---

## 📊 Color Usage Map

| Color Name | Where It's Used |
|---|---|
| `PrimaryBlue` | Buttons, active tabs, borders when focused, FAB |
| `TextPrimary` | Titles, note content |
| `TextSecondary` | Labels, chip text, descriptions |
| `TextTertiary` | Dates, subtle info |
| `TextOnPrimary` | Button text (white), text on gradients |
| `ErrorRed` | Error messages, logout button |
| `SuccessGreen` | "New Note" stats card, positive states |
| `GradientStart/End` | Dashboard banner, logo backgrounds |
| `BorderMedium` | Input field borders when unfocused |
| `CardBackground` | Upload cards background |
| `BorderLight` | Light input borders on SignUp/Settings |

---

## ⭐ Key Takeaway

> `Color.kt` is the **design bible** of the app. It has:
> - Light mode colors (default)
> - Dark mode colors (activated by Dark Mode toggle)
> - Ocean Blue theme colors (alternative theme)
> - Semantic colors (success green, error red, etc.)
>
> **Never write raw hex colors in screen files** — always use these named constants.
