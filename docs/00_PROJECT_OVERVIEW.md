# 📱 NoteHub — Complete Project Explanation
> **For complete beginners** — No coding experience needed!

---

## 🗂️ What is this project?

**NoteHub** is an Android app for taking notes. It is built using:
- **Kotlin** — the programming language (like the words of the recipe)
- **Jetpack Compose** — the UI toolkit (like the kitchen tools to build screens)
- **Material 3** — Google's design system (like a rule book for how things look)

---

## 📁 Project File Map

```
notehub/
│
├── app/src/main/java/com/example/notehub/
│   │
│   ├── 📄 MainActivity.kt          ← App's FRONT DOOR (starting point)
│   ├── 📄 MainScreen.kt            ← App's SHELL (navigation + top/bottom bar)
│   │
│   ├── navigation/
│   │   └── 📄 Screen.kt            ← All screen ROUTES (like a map of the app)
│   │
│   ├── screens/
│   │   ├── 📄 LoginScreen.kt       ← Login page
│   │   ├── 📄 SignUpScreen.kt      ← Create account page
│   │   ├── 📄 DashboardScreen.kt   ← Home/welcome page
│   │   ├── 📄 NotesScreen.kt       ← List of notes page
│   │   ├── 📄 AddNoteScreen.kt     ← Create new note page
│   │   ├── 📄 UploadsScreen.kt     ← File uploads page
│   │   └── 📄 SettingsScreen.kt    ← Settings page
│   │
│   └── ui/theme/
│       ├── 📄 Color.kt             ← All color definitions
│       ├── 📄 Theme.kt             ← Light/Dark theme setup
│       ├── 📄 ThemeManager.kt      ← Controls current theme
│       └── 📄 Type.kt              ← Font/text style definitions
```

---

## 🔄 How the App Flows

```
App Starts
    ↓
MainActivity.kt  (opens the app)
    ↓
MainScreen.kt    (sets up the navigation frame)
    ↓
LoginScreen.kt   (first screen user sees)
    ↓  (after login)
DashboardScreen.kt   (home screen)
    ↓  (tap bottom nav)
NotesScreen.kt / UploadsScreen.kt / SettingsScreen.kt
    ↓  (tap "+" button in Notes)
AddNoteScreen.kt
```

---

## 📚 Explanation Files Index

| File | Explanation Document |
|---|---|
| `MainActivity.kt` | `01_MainActivity_Explanation.md` |
| `MainScreen.kt` | `02_MainScreen_Explanation.md` |
| `Screen.kt` | `03_Screen_Explanation.md` |
| `LoginScreen.kt` | `04_LoginScreen_Explanation.md` |
| `SignUpScreen.kt` | `05_SignUpScreen_Explanation.md` |
| `DashboardScreen.kt` | `06_DashboardScreen_Explanation.md` |
| `NotesScreen.kt` | `07_NotesScreen_Explanation.md` |
| `AddNoteScreen.kt` | `08_AddNoteScreen_Explanation.md` |
| `UploadsScreen.kt` | `09_UploadsScreen_Explanation.md` |
| `SettingsScreen.kt` | `10_SettingsScreen_Explanation.md` |
| `Color.kt` | `11_Color_Explanation.md` |
| `Theme.kt` | `12_Theme_Explanation.md` |
| `ThemeManager.kt` | `13_ThemeManager_Explanation.md` |

---

## 🔑 Universal Glossary (Terms Used in Every File)

| Term | Simple Meaning |
|---|---|
| `package` | A folder label that tells Android where the file lives |
| `import` | Borrowing tools from Android's library before using them |
| `fun` | Short for **function** — a named block of code that does one job |
| `@Composable` | This function draws something on the screen |
| `var` | A variable that **can change** |
| `val` | A value that **cannot change** |
| `remember` | Keeps a value in memory so it doesn't reset |
| `mutableStateOf(...)` | A value that, when changed, automatically updates the UI |
| `Modifier` | A tool to change size, position, color, padding of any element |
| `dp` | Size unit for layouts — works the same on all phone sizes |
| `sp` | Size unit specifically for text |
| `Column` | Stacks items top-to-bottom |
| `Row` | Places items left-to-right |
| `Box` | Layers items on top of each other |
| `Spacer` | An invisible gap between elements |
| `Text(...)` | Displays text on screen |
| `Button(...)` | A clickable button |
| `Card(...)` | A rounded box with optional shadow |
| `Scaffold` | The basic screen frame (handles top bar, bottom bar, content area) |
| `@Preview` | Shows a preview of the screen in Android Studio only |
| `() -> Unit` | A mini-function/action that does something and returns nothing |
| `navController` | The GPS of the app — controls which screen is shown |

---

*📁 Project: NoteHub Android App*
*📖 Full explanation series for beginners*
