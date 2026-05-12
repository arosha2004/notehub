# 📄 LoginScreen.kt — Explanation

> This is the **Login page** — the first thing a user sees when they open the app.

---

## 🧠 What Does This Screen Show?

```
┌─────────────────────────────────┐
│           [Logo Icon]           │
│           NoteHub               │
│   Your premium note-taking...   │
│                                 │
│  ┌───────────────────────────┐  │
│  │  Welcome Back             │  │
│  │  Sign in to continue      │  │
│  │                           │  │
│  │  [Email Address field]    │  │
│  │  [Password field]     👁  │  │
│  │              Forgot Password?│
│  │                           │  │
│  │      [Sign In Button]     │  │
│  └───────────────────────────┘  │
│   Don't have account? Sign Up   │
└─────────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### Function Definition

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignUp: () -> Unit
) {
```

- `fun LoginScreen(...)` — Defines the login screen
- `onLoginSuccess` — An action to run when login succeeds (navigates to Dashboard)
- `onNavigateToSignUp` — An action to run when the user taps "Sign Up" (navigates to SignUp screen)

These are called **callbacks** — they're instructions passed in from `MainScreen.kt` that say "when login works, do THIS."

---

### State Variables

```kotlin
var email by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }
var passwordVisible by remember { mutableStateOf(false) }
var isLoading by remember { mutableStateOf(false) }
```

| Variable | Type | Starting Value | What it stores |
|---|---|---|---|
| `email` | Text | `""` (empty) | Whatever user types in the email field |
| `password` | Text | `""` (empty) | Whatever user types in the password field |
| `passwordVisible` | True/False | `false` (hidden) | Whether to show or hide the password |
| `isLoading` | True/False | `false` | Whether the sign-in spinner should show |

---

### Box with Gradient Background

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surfaceVariant
                )
            )
        ),
    contentAlignment = Alignment.Center
) {
```

- `Box` — A container that fills the whole screen
- `fillMaxSize()` — Takes up 100% of screen width and height
- `Brush.verticalGradient(...)` — Creates a smooth color gradient from top to bottom
- `listOf(background, surfaceVariant)` — Fades from the main background color to a slightly different shade going downward
- `contentAlignment = Alignment.Center` — Everything inside is centered on screen

> 🧠 **Analogy:** The gradient background is like a wall painted with two colors that smoothly blend into each other.

---

### Logo Section
 
```kotlin
Box(
    modifier = Modifier
        .size(80.dp)
        .background(
            brush = Brush.linearGradient(colors = listOf(GradientStart, GradientEnd)),
            shape = RoundedCornerShape(20.dp)
        ),
    contentAlignment = Alignment.Center
) {
    Image(
        painter = painterResource(id = R.drawable.icon),
        contentDescription = "NoteHub Logo",
        modifier = Modifier.size(56.dp)
    )
}
```

| Code | What it does |
|---|---|
| `size(80.dp)` | Logo container is 80dp × 80dp square |
| `Brush.linearGradient(...)` | Diagonal colour gradient on the logo box background |
| `RoundedCornerShape(20.dp)` | The corners are nicely rounded |
| `painterResource(id = R.drawable.icon)` | Loads the `icon` image from the `drawables` folder |
| `size(56.dp)` | The logo image itself is 56dp × 56dp (slightly smaller than the container) |

---

### App Title Text

```kotlin
Text(
    text = "NoteHub",
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onBackground,
    letterSpacing = (-0.5).sp
)

Text(
    text = "Your premium note-taking companion",
    fontSize = 15.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant,
    textAlign = TextAlign.Center,
    modifier = Modifier.padding(top = 8.dp, bottom = 48.dp),
    letterSpacing = 0.2.sp
)
```

- First `Text` — Big bold "NoteHub" title (36sp, Bold)
- `letterSpacing = (-0.5).sp` — Slightly tightens the letters together (looks more premium)
- Second `Text` — Subtitle tagline in smaller, lighter text
- `textAlign = TextAlign.Center` — Centers the text horizontally
- `padding(bottom = 48.dp)` — Adds space below before the card starts

---

### Login Card

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
) {
```

- `Card` — A container with rounded corners and a shadow
- `RoundedCornerShape(24.dp)` — Very rounded corners (looks modern)
- `elevation = 8.dp` — Adds a visible shadow effect under the card (makes it "float")
- Inside the card: the form fields and button

---

### Email Field

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Email Address") },
    placeholder = { Text("Enter your email") },
    modifier = Modifier.fillMaxWidth(),
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    shape = RoundedCornerShape(8.dp),
    colors = OutlinedTextFieldDefaults.colors(...)
)
```

| Code | What it does |
|---|---|
| `value = email` | Shows current value of `email` variable |
| `onValueChange = { email = it }` | Every keystroke updates `email` |
| `label = { Text("Email Address") }` | Floating label text above/inside the field |
| `placeholder = { Text("Enter your email") }` | Grey hint text shown when empty |
| `singleLine = true` | User can only type on one line |
| `keyboardType = KeyboardType.Email` | Opens the email keyboard (with @ key prominent) |
| `RoundedCornerShape(8.dp)` | Slightly rounded corners on the field |

---

### Password Field

```kotlin
OutlinedTextField(
    value = password,
    onValueChange = { password = it },
    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    trailingIcon = {
        IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                contentDescription = if (passwordVisible) "Hide password" else "Show password"
            )
        }
    },
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
)
```

| Code | What it does |
|---|---|
| `PasswordVisualTransformation()` | Replaces typed characters with dots (●●●●) |
| `VisualTransformation.None` | Shows text as normal (visible) |
| `if (passwordVisible) ... else ...` | Switches between showing and hiding based on the toggle |
| `trailingIcon = { IconButton(...) }` | Adds the 👁 eye icon on the right side |
| `onClick = { passwordVisible = !passwordVisible }` | Tapping the eye flips the visibility (true→false or false→true) |
| `Icons.Filled.Visibility` | Open eye icon (password visible) |
| `Icons.Filled.VisibilityOff` | Closed eye icon (password hidden) |

---

### Forgot Password Text

```kotlin
Text(
    text = "Forgot Password?",
    modifier = Modifier
        .align(Alignment.End)
        .clickable { /* Handle forgot password */ },
    color = MaterialTheme.colorScheme.primary
)
```

- `.align(Alignment.End)` — Pushes the text to the **right** side
- `.clickable { }` — Makes it tappable (currently no action attached)
- `color = primary` — Shows in the theme's primary color (blue/indigo)

---

### Sign In Button

```kotlin
Button(
    onClick = {
        isLoading = true
        onLoginSuccess()
    },
    modifier = Modifier.fillMaxWidth().height(56.dp),
    shape = RoundedCornerShape(16.dp),
    enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = TextOnPrimary)
    } else {
        Text("Sign In", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
```

| Code | What it does |
|---|---|
| `onClick = { isLoading = true; onLoginSuccess() }` | Sets loading spinner on, then calls the login success callback |
| `height(56.dp)` | Button is tall and easy to tap |
| `RoundedCornerShape(16.dp)` | Nicely rounded button shape |
| `enabled = email.isNotEmpty() && password.isNotEmpty() && !isLoading` | Button only works if BOTH fields filled AND not already loading |
| `if (isLoading) CircularProgressIndicator(...)` | Shows a spinning circle while loading |
| `else Text("Sign In")` | Shows the button text when not loading |

---

### Sign Up Link

```kotlin
Row(modifier = Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(text = "Don't have an account? ")
    Text(
        text = "Sign Up",
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onNavigateToSignUp() }
    )
}
```

- Two `Text` items inside a `Row` (side by side)
- "Don't have an account?" — plain grey text
- "Sign Up" — coloured, bold, **clickable** — calls `onNavigateToSignUp()`

---

## ⭐ Key Takeaway

> `LoginScreen.kt` builds the login UI with:
> - A gradient background
> - A floating logo with gradient background
> - A Card containing Email + Password fields + Sign In button
> - Password visibility toggle (👁 eye icon)
> - A link to Sign Up
> - The button only activates when both fields have text
