      # 🎓 NoteHub — Viva Preparation Guide
### Complete Study Guide with 50 Questions & Answers

> 📅 **Read this fully before your viva. 
Every answer is in simple, easy-to-speak language.**

---

## 📌 PART 1 — PROJECT OVERVIEW (Speak this first if asked "Tell me about your project")

> **Sample Answer:**
> *"Our project is NoteHub — an Android note-taking application built using Kotlin and Jetpack Compose. It allows users to register an account, log in, and manage their notes. The app has features like creating notes with categories and color labels, file uploads, settings with dark mode, and a dashboard with statistics. We used Material Design 3 for the UI and Jetpack Navigation for screen management."*

---

## 📌 PART 2 — TECHNOLOGY STACK (Know These Words!)

| Technology | What It Is | Why We Used It |
|---|---|---|
| **Kotlin** | The programming language | Modern, safe, and officially recommended by Google for Android |
| **Jetpack Compose** | UI toolkit to build screens | Allows building UI with functions instead of XML files |
| **Material Design 3** | Google's design system | Gives ready-made buttons, cards, text fields with good looks |
| **Navigation Component** | Screen navigation library | Handles moving between screens (like a router) |
| **Android Studio** | The IDE (code editor) | Official tool for Android development |

---

## 📌 PART 3 — APP SCREENS QUICK REFERENCE

| Screen | File Name | What It Does |
|---|---|---|
| Login | `LoginScreen.kt` | User signs in |
| Sign Up | `SignUpScreen.kt` | User creates new account |
| Dashboard | `DashboardScreen.kt` | Home screen with stats and quick actions |
| Notes | `NotesScreen.kt` | List of all notes |
| Add Note | `AddNoteScreen.kt` | Form to create a new note |
| Uploads | `UploadsScreen.kt` | File uploads page |
| Settings | `SettingsScreen.kt` | Profile, dark mode, logout |

---

## 📌 PART 4 — KEY CONCEPTS TO REMEMBER

### 🔹 What is `@Composable`?
A function that **draws something on the screen**. Any UI element in Jetpack Compose is a `@Composable` function.

### 🔹 What is `remember`?
Keeps a variable's value **in memory** so it doesn't reset when the screen redraws.

### 🔹 What is `mutableStateOf`?
Creates a variable that, when **changed**, automatically **updates the UI**.

### 🔹 What is `NavController`?
The **GPS of the app** — it controls which screen is shown.

### 🔹 What is `Scaffold`?
A ready-made screen frame that provides slots for `topBar`, `bottomBar`, `floatingActionButton`, and `content`.

### 🔹 What is `LazyColumn`?
A **smart scrollable list** that only renders items visible on screen — better performance than regular Column.

### 🔹 What is a `data class`?
A class designed purely to **hold data** (like a form template). Example: `data class Note(id, title, content...)`.

### 🔹 What is a `sealed class`?
A class where **all possible subclasses are defined inside it** — like a fixed menu of options. Used for `Screen` routes.

### 🔹 What is a `singleton (object)`?
A class that has **only ONE instance** in the entire app. `ThemeManager` is a singleton.

### 🔹 What is `enum class`?
A type that can only be **one of a fixed set of values**. Example: `AppTheme.INDIGO` or `AppTheme.OCEAN`.

---

---

# 🧠 50 VIVA QUESTIONS AND ANSWERS

---

## 🟦 SECTION A — Basic Project Questions (Q1–Q10)

---

**Q1. What is NoteHub?**

> NoteHub is an Android note-taking application that allows users to create, manage, and organize their notes. It also supports file uploads and has customizable themes including dark mode.

---

**Q2. What programming language did you use?**

> We used **Kotlin** — the officially recommended language by Google for Android development. It is modern, concise, and safe compared to Java.

---

**Q3. What is Jetpack Compose?**

> Jetpack Compose is a **modern UI toolkit** for Android. Instead of writing XML layout files, we write UI using Kotlin functions marked with `@Composable`. It makes building screens simpler and faster.

---

**Q4. What is the starting point of an Android app?**

> The starting point is **`MainActivity.kt`**. Android calls its `onCreate()` function when the app is opened. Inside it, we call `setContent {}` to render our UI.

---

**Q5. How many screens does your app have?**

> Our app has **7 screens**:
> 1. Login Screen
> 2. Sign Up Screen
> 3. Dashboard Screen
> 4. Notes Screen
> 5. Add Note Screen
> 6. Uploads Screen
> 7. Settings Screen

---

**Q6. What is Material Design 3?**

> Material Design 3 (MD3) is Google's latest design system. It provides pre-built, beautifully designed UI components like buttons, cards, input fields, and navigation bars. We used MD3 components throughout the NoteHub app.

---

**Q7. What IDE did you use to build the app?**

> We used **Android Studio** — the official IDE for Android development. It provides tools for coding, designing, debugging, and running the app on emulators or real devices.

---

**Q8. What is `setContent { }` in MainActivity?**

> `setContent { }` is a function that tells Android **what UI to display on the screen**. Everything inside the curly braces becomes the app's visual content.

---

**Q9. What is `enableEdgeToEdge()`?**

> It is a function called in `MainActivity` that makes the app **draw all the way to the screen edges** — behind the status bar and navigation bar — for a full-screen, immersive look.

---

**Q10. What is a Package in Kotlin?**

> A package is like a **folder label** that tells Android where the file lives in the project. For example, `package com.example.notehub.screens` means the file is inside the screens folder.

---

## 🟩 SECTION B — Navigation Questions (Q11–Q18)

---

**Q11. How does navigation work in your app?**

> We use **Jetpack Navigation Component**. Every screen has a unique route string (like `"dashboard"`). The `NavController` manages which screen is shown. When the user taps a button, we call `navController.navigate("route")` to go to that screen.

---

**Q12. What is `NavController`?**

> `NavController` is the **GPS of the app**. It keeps track of which screen is currently shown and handles moving between screens. We create it using `rememberNavController()`.

---

**Q13. What is `NavHost`?**

> `NavHost` is the **container** that holds all screen routes. Inside it, we use `composable("route") { Screen() }` to register each screen. It decides which screen to show based on the current route.

---

**Q14. What is the `Screen.kt` file for?**

> `Screen.kt` defines all the **route names** as a `sealed class`. Instead of writing raw strings like `"dashboard"` everywhere (which could cause typos), we use `Screen.Dashboard.route`. It also stores each screen's title and icon.

---

**Q15. What is a `sealed class`? Why did you use it for Screen?**

> A `sealed class` is a class where **all possible subclasses must be defined inside it** — like a fixed menu. We used it for `Screen` so that we have a controlled, safe list of all possible screens in the app. If we miss a screen, the code won't compile.

---

**Q16. What is `popUpTo()` in navigation?**

> `popUpTo()` **removes screens from the back stack** when navigating. For example, after logging in, we use `popUpTo(Screen.Login.route)` with `inclusive = true` so the user can't press Back to return to the Login screen.

---

**Q17. What is `launchSingleTop = true`?**

> It prevents **creating a duplicate copy** of a screen. If the user is already on Dashboard and taps the Dashboard tab again, it won't open a second Dashboard on top — it stays on the existing one.

---

**Q18. When is the bottom navigation bar hidden?**

> The bottom bar is hidden on the **Login, Sign Up, and Add Note screens** because those screens have their own navigation controls. We use `Screen.shouldShowDrawer(currentRoute)` to check this — it returns `false` for those 3 screens.

---

## 🟨 SECTION C — UI & Compose Questions (Q19–Q28)

---

**Q19. What is a `@Composable` function?**

> A `@Composable` function is a **function that draws UI on the screen**. In Jetpack Compose, everything visible — text, buttons, images, cards — is a `@Composable` function. You mark it with the `@Composable` annotation above `fun`.

---

**Q20. What is `remember { }`?**

> `remember { }` **keeps a value in memory** so it doesn't get reset every time the screen redraws. For example, `remember { mutableStateOf("") }` keeps our email text safe even when the screen updates.

---

**Q21. What is `mutableStateOf`?**

> `mutableStateOf` creates a **reactive variable** — when its value changes, Compose **automatically redraws** any part of the UI that uses it. This is how typing in a text field instantly updates the displayed text.

---

**Q22. What is `Scaffold`?**

> `Scaffold` is a **pre-built screen layout** from Material 3. It provides designated areas for `topBar`, `bottomBar`, `floatingActionButton`, and the main `content`. We used it in `AddNoteScreen` and `NotesScreen`.

---

**Q23. What is the difference between `Column`, `Row`, and `Box`?**

> - **`Column`** — Arranges children **vertically** (top to bottom)
> - **`Row`** — Arranges children **horizontally** (left to right)
> - **`Box`** — **Layers** children on top of each other

---

**Q24. What is `LazyColumn`?**

> `LazyColumn` is a **high-performance scrollable list**. Unlike a regular `Column`, it only renders the items currently visible on screen — not all items at once. This makes it efficient for long lists of notes.

---

**Q25. What is a `Card` in Compose?**

> A `Card` is a **container with rounded corners and optional shadow** (elevation). It's used to group related content visually. We used `Card` for note cards, stat boxes, settings sections, and more.

---

**Q26. What is `FloatingActionButton (FAB)`?**

> A `FloatingActionButton` is a **round button that floats over content**, usually in the bottom-right corner. In `NotesScreen` and `UploadsScreen`, we use it as the **"+" Add** button.

---

**Q27. What is `OutlinedTextField`?**

> An `OutlinedTextField` is a **text input box with an outline border**. It shows a floating label and supports features like keyboard type, error state, and trailing icons (like the 👁 password toggle).

---

**Q28. What is `PasswordVisualTransformation`?**

> It is a transformation applied to a text field that **replaces all characters with dots (●●●●)** to hide the password. When the user taps the eye icon, we switch to `VisualTransformation.None` to show the real text.

---

## 🟧 SECTION D — Screens-Specific Questions (Q29–40)

---

**Q29. How does the Login button get enabled/disabled?**

> The Sign In button is enabled only when:
> - Email field is **not empty**
> - Password field is **not empty**
> - App is **not currently loading**
>
> ```kotlin
> enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
> ```

---

**Q30. How is the password visibility toggle implemented?**

> We use a `var passwordVisible by remember { mutableStateOf(false) }` state. The eye `IconButton` flips it: `passwordVisible = !passwordVisible`. The field uses:
> ```kotlin
> visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
> ```

---

**Q31. What validation is done in the Sign Up screen?**

> - All 4 fields (Name, Email, Password, Confirm Password) must be **filled**
> - The **password and confirm password must match**
> - If they don't match, a red error message "Passwords do not match" appears below the confirm field
> - The Create Account button stays **disabled** until all conditions pass

---

**Q32. What does the Dashboard screen show?**

> The Dashboard shows:
> 1. **WelcomeCard** — A gradient banner with a "Create New Note" button
> 2. **StatsGrid** — A 2×2 grid showing Total Notes, This Month, Categories, and Uploads count
> 3. **QuickActionsSection** — Two colored buttons: "New Note" and "Upload File"

---

**Q33. What is a `data class Note`?**

> It's a **blueprint** that defines what information one note contains:
> - `id` (number), `title` (text), `content` (text), `date` (text), `category` (text), `color` (Color)
>
> Every note object created from this class holds these 6 values.

---

**Q34. How are notes displayed in the Notes screen?**

> Notes are displayed using a `LazyColumn`. For each `Note` in the list, a `NoteCard()` composable is called. Each card shows a **colored left bar**, category tag, title, content preview, and date.

---

**Q35. What happens when the Notes list is empty?**

> When `notes.isEmpty()` is true, a centered message `"No notes yet"` is shown instead of the list. This is called an **empty state**.

---

**Q36. What fields are in the Add Note screen?**

> - **Category selector** — Filter chips (Study, Work, Personal, Ideas)
> - **Title field** — Single-line text input
> - **Content field** — Multi-line text area
> - **Color picker** — Selects a color for the note
> - **Save button** — In the top bar; only enabled when title AND content are filled

---

**Q37. How does the Save button work in Add Note screen?**

> The Save `TextButton` in the TopAppBar is enabled only when both `title` and `content` are not blank:
> ```kotlin
> enabled = title.isNotBlank() && content.isNotBlank()
> ```
> Currently, clicking it just navigates back (actual saving to a database is not yet implemented).

---

**Q38. What does the Uploads screen show currently?**

> Currently, the upload list is **empty** (`emptyList<Upload>()`), so the screen shows:
> - A blue header card saying "Uploads — 0 files"
> - A centered "No uploads yet" message
> - A floating upload button (does nothing yet)

---

**Q39. What sections are in the Settings screen?**

> 1. **Profile Settings** — Edit name and email, change photo
> 2. **Change Password** — Current, New, Confirm password fields
> 3. **Appearance** — Dark Mode toggle (fully functional)
> 4. **Preferences** — Notifications toggle
> 5. **About** — App version, Privacy Policy, Terms of Service
> 6. **Log Out Button** — Navigates back to Login, clears back stack

---

**Q40. How does the Logout button work?**

> The Log Out button calls the `onLogout()` callback:
> ```kotlin
> onClick = onLogout
> ```
> In `MainScreen.kt`, `onLogout` is defined as:
> ```kotlin
> navController.navigate(Screen.Login.route) { popUpTo(0) }
> ```
> `popUpTo(0)` clears **all screens** from history so the user can't press Back after logging out.

---

## 🟥 SECTION E — Theme and Architecture Questions (Q41–50)

---

**Q41. How does the Dark Mode work in your app?**

> 1. The user taps the Dark Mode toggle in Settings
> 2. `ThemeManager.setDarkMode(true)` is called
> 3. `ThemeManager.isDarkMode` changes (it uses `mutableStateOf`)
> 4. `NoteHubTheme()` in `Theme.kt` reads this value and picks `IndigoDarkColorScheme`
> 5. The entire app **automatically redraws** with dark colors

---

**Q42. What is `ThemeManager`?**

> `ThemeManager` is a **singleton object** that stores the app's current theme state (Indigo or Ocean) and dark mode state (on or off). Because it uses `mutableStateOf`, any change to it **automatically updates the UI** everywhere.

---

**Q43. What is a Singleton? Why is ThemeManager a singleton?**

> A singleton is a class that has **only ONE instance** in the entire app. `ThemeManager` is a singleton (created with `object`) because we need **one central place** to control the theme — if we had multiple instances, they might conflict with each other.

---

**Q44. What themes does your app support?**

> The app supports **4 color schemes**:
> 1. **Indigo Light** (default) — Purple/indigo colors with white background
> 2. **Indigo Dark** — Same indigo theme with dark purple background
> 3. **Ocean Light** — Cyan/blue theme with light background
> 4. **Ocean Dark** — Cyan/blue theme with dark background

---

**Q45. What is `NoteHubTheme()`?**

> `NoteHubTheme()` is the **top-level theme wrapper** that wraps the entire app. It reads the current theme and dark mode from `ThemeManager`, selects the right color scheme, and passes it to Material 3's `MaterialTheme()`. All screens automatically use the right colors.

---

**Q46. What is `Color.kt` used for?**

> `Color.kt` defines **every color** used in the app as named constants. Instead of writing raw hex values like `#6366F1` in every file, we write `PrimaryBlue` — one name, one place to update if the color changes.

---

**Q47. What is the difference between `var` and `val`?**

> - **`var`** — Variable that **CAN be changed** (mutable)
> - **`val`** — Value that **CANNOT be changed** (immutable/read-only)
>
> Example: `var email = ""` can be updated as the user types. `val route = "login"` stays fixed.

---

**Q48. What does `Modifier` do?**

> `Modifier` is used to **change the appearance or behavior** of any composable. You can chain multiple modifiers:
> ```kotlin
> Modifier
>     .fillMaxWidth()    // 100% width
>     .padding(16.dp)    // 16dp space around
>     .background(Red)   // red background
>     .clickable { }     // make tappable
> ```

---

**Q49. What is `dp` and `sp`?**

> - **`dp` (density-independent pixels)** — Used for sizes and spacing. Works the same on all phone screen sizes.
> - **`sp` (scale-independent pixels)** — Used specifically for **text size**. It also respects the user's font size settings in the phone.

---

**Q50. What are the limitations or future improvements of NoteHub?**

> **Current limitations:**
> - Notes are **hardcoded** — not saved to a real database
> - Authentication is **simulated** — no real server login
> - Upload functionality is **not implemented** yet
> - Dashboard stats show **hardcoded numbers**
>
> **Future improvements:**
> - Add **Room Database** to save notes permanently
> - Add **Firebase** for real authentication
> - Implement **file upload** using Storage APIs
> - Add **search and filter** for notes
> - Add **note editing and deletion**

---

---

# 🚀 QUICK CHEAT SHEET — 1 Minute Before Viva

```
Project  : NoteHub — Android Note-Taking App
Language : Kotlin
UI       : Jetpack Compose + Material Design 3
Screens  : Login, SignUp, Dashboard, Notes, AddNote, Uploads, Settings
Nav      : NavController + NavHost + sealed class Screen
Theme    : ThemeManager (singleton) + NoteHubTheme + Color.kt
Data     : Hardcoded (Room Database not yet added)
Entry    : MainActivity.onCreate() → MainScreen() → AppNavHost()
Dark mode: ThemeManager.setDarkMode() → NoteHubTheme picks dark scheme
Key concepts: @Composable, remember, mutableStateOf, LazyColumn, Scaffold
```

---

# 💬 PHRASES TO USE IN VIVA

- *"We used Jetpack Compose which is the modern declarative UI toolkit..."*
- *"Navigation is handled by the Jetpack Navigation Component..."*
- *"State is managed using `mutableStateOf` which automatically triggers recomposition..."*
- *"The `ThemeManager` is a singleton that holds global theme state..."*
- *"Currently the data is hardcoded, but in future we plan to integrate Room Database..."*
- *"The `sealed class Screen` ensures type-safe navigation routes..."*
- *"Material Design 3 components were used for a consistent and modern UI..."*

---

> ✅ **All the best for your viva! You've got this! 💪**
> 
> 📁 *NoteHub Project — Viva Guide 2026*
