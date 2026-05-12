# 📄 SignUpScreen.kt — Explanation

> This is the **Create Account page** — where new users register for NoteHub.

---

## 🧠 What Does This Screen Show?

```
┌─────────────────────────────────┐
│           [Logo Icon]           │
│           NoteHub               │
│  Start your premium journey...  │
│                                 │
│  ┌───────────────────────────┐  │
│  │  Create Account           │  │
│  │  Join thousands of users  │  │
│  │                           │  │
│  │  [Full Name field]        │  │
│  │  [Email Address field]    │  │
│  │  [Password field]     👁  │  │
│  │  [Confirm Password]   👁  │  │
│  │  ⚠ Passwords do not match │  │
│  │                           │  │
│  │   [Create Account Button] │  │
│  └───────────────────────────┘  │
│   Already have account? Login   │
└─────────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### Function Definition

```kotlin
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
```

- `onSignUpSuccess` — Runs when registration works → navigates to Dashboard
- `onNavigateToLogin` — Runs when user taps "Login" link → goes back to Login screen

---

### State Variables

```kotlin
var fullName by remember { mutableStateOf("") }
var email by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }
var confirmPassword by remember { mutableStateOf("") }
var passwordVisible by remember { mutableStateOf(false) }
var confirmPasswordVisible by remember { mutableStateOf(false) }
var isLoading by remember { mutableStateOf(false) }
```

| Variable | What it stores |
|---|---|
| `fullName` | User's full name as they type it |
| `email` | User's email address |
| `password` | User's chosen password |
| `confirmPassword` | Password typed a second time to confirm it matches |
| `passwordVisible` | Whether to show/hide the password field |
| `confirmPasswordVisible` | Whether to show/hide the confirm password field |
| `isLoading` | Whether the registration spinner should show |

> SignUpScreen has **2 password visibility toggles** (one for each field).

---

### Background (Gradient)

```kotlin
Box(
    modifier = Modifier
        .fillMaxSize()
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF8F7FF), Color(0xFFEEECFF))
            )
        )
        .verticalScroll(rememberScrollState()),
    contentAlignment = Alignment.Center
)
```

- Similar to LoginScreen but uses **hard-coded hex colors** instead of theme colors
- `Color(0xFFF8F7FF)` — A very light purple-white at the top
- `Color(0xFFEEECFF)` — A slightly more purple shade at the bottom
- `verticalScroll(rememberScrollState())` — Makes the screen **scrollable** because there are many fields that might not all fit on small screens

---

### 4 Input Fields

The SignUpScreen has **4 text fields**, each following the same pattern:

#### 1. Full Name Field
```kotlin
OutlinedTextField(
    value = fullName,
    onValueChange = { fullName = it },
    label = { Text("Full Name") },
    placeholder = { Text("Enter your full name") },
    singleLine = true,
    shape = RoundedCornerShape(8.dp)
)
```
Plain text field. No special keyboard needed.

#### 2. Email Field
```kotlin
keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
```
Opens the email keyboard (shows @ character prominently).

#### 3. Password Field
```kotlin
visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
trailingIcon = {
    IconButton(onClick = { passwordVisible = !passwordVisible }) {
        Icon(imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff)
    }
}
```
- Hides characters as dots by default
- Has a 👁 eye icon to toggle visibility

#### 4. Confirm Password Field
```kotlin
isError = confirmPassword.isNotEmpty() && password != confirmPassword
```
Same as Password field, PLUS an `isError` flag:
- `isError = true` → field turns red if the confirm password doesn't match the password
- This only activates **after the user starts typing** in the confirm field

---

### Password Mismatch Warning

```kotlin
if (confirmPassword.isNotEmpty() && password != confirmPassword) {
    Text(
        text = "Passwords do not match",
        fontSize = 12.sp,
        color = ErrorRed,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
    )
}
```

- This `if` block only shows a red error message if:
  1. The user has typed something in the confirm field (`isNotEmpty()`)
  2. AND it doesn't match the password (`password != confirmPassword`)
- The error message appears **below** the confirm password field
- `ErrorRed` is our theme's red error color

---

### Create Account Button

```kotlin
Button(
    onClick = {
        isLoading = true
        onSignUpSuccess()
    },
    enabled = fullName.isNotEmpty() &&
              email.isNotEmpty() &&
              password.isNotEmpty() &&
              confirmPassword.isNotEmpty() &&
              password == confirmPassword &&
              !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(...)
    } else {
        Text("Create Account", fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
```

The button is only enabled when ALL of these are true:
1. ✅ Full name is not empty
2. ✅ Email is not empty
3. ✅ Password is not empty
4. ✅ Confirm password is not empty
5. ✅ Both passwords match
6. ✅ Not currently loading

If any condition fails → button is greyed out and unclickable.

---

### Login Link

```kotlin
Row(modifier = Modifier.padding(top = 24.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(text = "Already have an account? ")
    Text(
        text = "Login",
        color = PrimaryBlue,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clickable { onNavigateToLogin() }
    )
}
```

Two texts side by side — "Already have an account?" + a clickable blue "Login" link.

---

## 🆚 Difference Between LoginScreen and SignUpScreen

| Feature | LoginScreen | SignUpScreen |
|---|---|---|
| Fields | Email + Password | Full Name + Email + Password + Confirm Password |
| Password toggles | 1 | 2 |
| Scroll | Not needed | ✅ Scrollable (more content) |
| Error message | None | ✅ Password mismatch warning |
| Button label | Sign In | Create Account |
| Button enabled condition | Both fields filled | All 4 filled + passwords match |

---

## ⭐ Key Takeaway

> `SignUpScreen.kt` is like LoginScreen but with **more fields and validation**. The key new feature is the **password confirmation check** — if the two passwords don't match, the button stays disabled and a red error shows.
