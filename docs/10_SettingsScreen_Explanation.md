# 📄 SettingsScreen.kt — Explanation

> This is the **Settings page** — where users manage their profile, password, appearance, and log out.

---

## 🧠 What Does This Screen Show?

```
┌──────────────────────────────────────┐
│  ┌────────────────────────────────┐  │
│  │  Profile Settings              │  │  ← Card 1
│  │  [Circle Profile Photo]        │  │
│  │  [Change Photo]                │  │
│  │  [Full Name field]             │  │
│  │  [Email Address field]         │  │
│  │  [Save Changes button]         │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Change Password               │  │  ← Card 2
│  │  [Current Password] 👁         │  │
│  │  [New Password] 👁             │  │
│  │  [Confirm New Password] 👁     │  │
│  │  [Update Password button]      │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Appearance                    │  │  ← Card 3
│  │  🌙 Dark Mode           [○●]   │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  Preferences                   │  │  ← Card 4
│  │  🔔 Notifications       [○●]   │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │  About                         │  │  ← Card 5
│  │  Version           1.0.0       │  │
│  │  Privacy Policy    View >      │  │
│  │  Terms of Service  View >      │  │
│  └────────────────────────────────┘  │
│  [🚪 Log Out]                        │  ← Logout Button
└──────────────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### Function Definition

```kotlin
@Composable
fun SettingsScreen(onLogout: () -> Unit) {
```

- `onLogout` — An action passed from `MainScreen.kt`. When called, it navigates back to the Login screen and clears all back history.

---

### State Variables

```kotlin
var fullName by remember { mutableStateOf("Arosha") }
var email by remember { mutableStateOf("Arosha@gmail.com.com") }
var currentPassword by remember { mutableStateOf("") }
var newPassword by remember { mutableStateOf("") }
var confirmPassword by remember { mutableStateOf("") }

var currentPasswordVisible by remember { mutableStateOf(false) }
var newPasswordVisible by remember { mutableStateOf(false) }
var confirmPasswordVisible by remember { mutableStateOf(false) }

var notificationsEnabled by remember { mutableStateOf(true) }
```

| Variable | Default Value | What it stores |
|---|---|---|
| `fullName` | `"Arosha"` | User's display name (pre-filled) |
| `email` | `"Arosha@gmail.com.com"` | User's email (pre-filled) |
| `currentPassword` | `""` | Current password input |
| `newPassword` | `""` | New password input |
| `confirmPassword` | `""` | Confirm new password input |
| `currentPasswordVisible` | `false` | Show/hide current password |
| `newPasswordVisible` | `false` | Show/hide new password |
| `confirmPasswordVisible` | `false` | Show/hide confirm password |
| `notificationsEnabled` | `true` | Whether notifications toggle is ON |

---

### Card 1 — Profile Settings

```kotlin
Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(2.dp)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Profile Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // Profile Picture (circular)
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(secondary)
        ) {
            Image(painter = painterResource(R.drawable.icon), contentScale = ContentScale.Crop)
        }

        TextButton(onClick = { /* Change profile picture */ }) {
            Text("Change Photo", color = primary)
        }

        // Full Name Field
        OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") })

        // Email Field
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))

        // Save button
        Button(onClick = { /* Save profile changes */ }) {
            Text("Save Changes")
        }
    }
}
```

| Code | What it does |
|---|---|
| `CircleShape` | Makes the profile picture box perfectly circular |
| `ContentScale.Crop` | Crops the image to fill the circle without distorting it |
| `TextButton(onClick = { })` | "Change Photo" button — currently does nothing |
| `fullName` pre-filled with "Arosha" | Shows a default name in the field |
| `Button(onClick = { })` | Save button — currently does nothing (no database connected) |

---

### Card 2 — Change Password

```kotlin
// Current Password Field
OutlinedTextField(
    value = currentPassword,
    onValueChange = { currentPassword = it },
    label = { Text("Current Password") },
    visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    trailingIcon = {
        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
            Icon(if (currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff)
        }
    }
)
// ... (same pattern for New Password and Confirm New Password)

// Password mismatch error
if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
    Text("Passwords do not match", color = ErrorRed)
}

// Update button
Button(
    enabled = currentPassword.isNotEmpty() &&
              newPassword.isNotEmpty() &&
              confirmPassword.isNotEmpty() &&
              newPassword == confirmPassword
) {
    Text("Update Password")
}
```

- Has 3 password fields, each with a 👁 toggle — same pattern as SignUpScreen
- The "Update Password" button is only enabled when:
  1. All 3 fields filled
  2. New password matches confirm password
- Currently does nothing on click (no backend)

---

### Card 3 — Appearance (Dark Mode Toggle)

```kotlin
SettingItem(
    icon = Icons.Filled.DarkMode,
    title = "Dark Mode",
    description = "Switch between light and dark theme",
    isChecked = ThemeManager.isDarkMode,
    onCheckedChange = { ThemeManager.setDarkMode(it) }
)
```

- Uses `ThemeManager.isDarkMode` to read current dark mode state
- `ThemeManager.setDarkMode(it)` — When the toggle changes, it updates the theme globally for the whole app
- This is the only **fully functional** setting in the app!

---

### Card 4 — Preferences (Notifications Toggle)

```kotlin
SettingItem(
    icon = Icons.Filled.Notifications,
    title = "Notifications",
    description = "Receive push notifications",
    isChecked = notificationsEnabled,
    onCheckedChange = { notificationsEnabled = it }
)
```

- Toggles `notificationsEnabled` state
- Currently **visual only** — doesn't actually enable/disable real notifications

---

### Card 5 — About Section

```kotlin
AboutItem(title = "Version", value = "1.0.0")
AboutItem(title = "Privacy Policy", value = "View", isClickable = true)
AboutItem(title = "Terms of Service", value = "View", isClickable = true)
```

Three rows showing app information. "View" items are clickable (show a > arrow) but currently do nothing.

---

### Logout Button

```kotlin
Button(
    onClick = onLogout,
    modifier = Modifier.fillMaxWidth().height(50.dp),
    colors = ButtonDefaults.buttonColors(
        containerColor = ErrorRed.copy(alpha = 0.1f),
        contentColor = ErrorRed
    ),
    shape = RoundedCornerShape(12.dp),
    elevation = ButtonDefaults.buttonElevation(0.dp)
) {
    Icon(Icons.Filled.Logout, contentDescription = "Logout")
    Spacer(modifier = Modifier.width(8.dp))
    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
}
```

| Code | What it does |
|---|---|
| `onClick = onLogout` | Calls the logout callback from MainScreen → navigates to Login |
| `containerColor = ErrorRed.copy(alpha = 0.1f)` | Very light RED button background (not fully red, just a tint) |
| `contentColor = ErrorRed` | Text and icon are red |
| `elevation = 0.dp` | No shadow — flat appearance |
| `Icons.Filled.Logout` | Door/exit icon |

---

### SettingItem() — Reusable Toggle Row

```kotlin
@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, ...)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold)
            Text(text = description, fontSize = 13.sp, color = onSurfaceVariant)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextOnPrimary,
                checkedTrackColor = PrimaryBlue,
                uncheckedThumbColor = TextOnPrimary,
                uncheckedTrackColor = BorderMedium
            )
        )
    }
}
```

This is a **reusable component** used for both Dark Mode and Notifications:

```
[Icon]  Title Text              [Toggle ○|●]
        Description text
```

| Code | What it does |
|---|---|
| `Column(weight = 1f)` | Text takes all space between icon and toggle |
| `Switch(checked = isChecked, onCheckedChange = ...)` | The toggle switch component |
| `checkedTrackColor = PrimaryBlue` | When ON, the track (background of toggle) is blue |
| `checkedThumbColor = TextOnPrimary` | When ON, the circle thumb is white |
| `uncheckedTrackColor = BorderMedium` | When OFF, the track is grey |

---

### AboutItem() — Info Row

```kotlin
@Composable
fun AboutItem(title: String, value: String, isClickable: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (isClickable) Modifier.clickable { } else Modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                color = if (isClickable) primary else onSurfaceVariant,
                fontWeight = if (isClickable) FontWeight.Medium else FontWeight.Normal
            )
            if (isClickable) {
                Icon(imageVector = Icons.Filled.ChevronRight, tint = primary)
            }
        }
    }
}
```

| Code | What it does |
|---|---|
| `Arrangement.SpaceBetween` | Title on left, value on right |
| `.then(if (isClickable) Modifier.clickable { } else Modifier)` | Conditionally adds click behavior |
| `if (isClickable) Icon(ChevronRight)` | Shows a ">" arrow only on clickable items |
| `color = if (isClickable) primary else onSurfaceVariant` | Clickable items text is blue, static items are grey |

---

## ⭐ Key Takeaway

> `SettingsScreen.kt` is the most complex screen with **5 cards + logout button**. It has:
> - Profile editing (name + email)
> - Password change with validation
> - **Functional Dark Mode toggle** (the only fully working setting)
> - Notifications toggle (visual only)
> - About info (version, policies)
> - **Logout button** that properly navigates back to Login and clears back stack
