# 📄 MainActivity.kt — Explanation

> This is the **front door** of the entire app. Every Android app must have one.

---

## 📋 Full File Content

```kotlin
package com.example.notehub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notehub.ui.theme.NoteHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoteHubTheme {
                MainScreen()
            }
        }
    }
}
```

---

## 🔍 Line-by-Line Explanation

---

### Line 1 — Package
```kotlin
package com.example.notehub
```
This file lives in the main `notehub` package (main folder of the app). Think of it like writing your address on a letter.

---

### Lines 3–7 — Imports
```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.notehub.ui.theme.NoteHubTheme
```

| Import | What it gives us |
|---|---|
| `Bundle` | A container for saving/restoring screen state on rotation |
| `ComponentActivity` | The base class every modern Android screen must extend |
| `setContent` | The function that tells Android "draw this Compose UI here" |
| `enableEdgeToEdge` | Makes the app draw all the way to screen edges (no white bars) |
| `NoteHubTheme` | Our custom color/font theme |

---

### Lines 9 — Class Definition
```kotlin
class MainActivity : ComponentActivity() {
```
- `class MainActivity` — Creates a class (a blueprint) called `MainActivity`
- `: ComponentActivity()` — Inherits all the powers of Android's activity system
- Think of this like: **"This is a screen. It borrows all the standard screen features from Android."**

---

### Lines 10–11 — onCreate Function
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
```
- `override fun onCreate(...)` — This is a function Android calls **automatically** when the app first opens. You're overriding (replacing) the default behavior.
- `super.onCreate(...)` — First, run the parent's version of onCreate (required — never skip this!)
- `savedInstanceState` — Data saved from before (e.g., if the phone rotated). Can be `null` if there's nothing saved.

> 🧠 **Analogy:** `onCreate` is like the "morning routine" that runs every time you wake up (open the app).

---

### Line 12 — Edge-to-Edge Display
```kotlin
enableEdgeToEdge()
```
Makes the app fill the entire screen including behind the status bar (the bar with the time/battery). Without this, there would be a grey system bar at the top.

---

### Lines 13–17 — Set the UI Content
```kotlin
setContent {
    NoteHubTheme {
        MainScreen()
    }
}
```
- `setContent { }` — Tells Android: "Everything inside these curly braces is what to show on the screen"
- `NoteHubTheme { }` — Wraps everything in our custom colors and fonts
- `MainScreen()` — Calls the main screen function (which has the navigation, top bar, bottom bar, etc.)

> 🧠 **Analogy:** `setContent` is like saying "open the curtains and show THIS on stage." `NoteHubTheme` is the paint and lighting. `MainScreen()` is the actual stage content.

---

## 🗺️ Visual Summary

```
Android System
      ↓  (app launched)
MainActivity.onCreate()
      ↓
enableEdgeToEdge()   → Full screen mode
      ↓
setContent { }       → Draw the UI
      ↓
NoteHubTheme { }     → Apply colors/fonts
      ↓
MainScreen()         → Draw the actual app shell
```

---

## ⭐ Key Takeaway

> `MainActivity.kt` is the **entry point** of the app. It does 3 things:
> 1. Turns on edge-to-edge display
> 2. Applies the NoteHub theme
> 3. Launches `MainScreen` as the starting UI

It is intentionally short — all the real work is in `MainScreen.kt` and the screen files.
