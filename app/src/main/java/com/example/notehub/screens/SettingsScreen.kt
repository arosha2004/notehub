package com.example.notehub.screens
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.R
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

/**
 * SettingsScreen — full settings and profile management screen.
 *
 * @param onLogout  Called when the user taps "Log Out" — navigates back to LoginScreen
 */
@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    // ── STATE VARIABLES ────────────────────────────────────────────

    // Profile fields (pre-filled with current user data)
    var fullName by remember { mutableStateOf("Arosha") }
    var email by remember { mutableStateOf("Arosha@gmail.com") }

    // Password change fields (all start empty)
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Visibility toggles for the 3 password fields
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Toggle state for notifications (starts enabled)
    var notificationsEnabled by remember { mutableStateOf(true) }

    // Snackbar host for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    Icons.Filled.Logout,
                    contentDescription = null,
                    tint = ErrorRed,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    "Log Out",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to log out? You'll need to sign in again to access your notes.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorRed,
                        contentColor = TextOnPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Scaffold with SnackbarHost for feedback messages
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        // Root Column — fills screen, has background color, scrollable vertically
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()) // entire screen is scrollable
                .padding(16.dp)
        ) {

            // SECTION 1 — PROFILE SETTINGS
            // Allows user to change name, email, and profile photo

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Section heading
                    Text(
                        text = "Profile Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    // ── PROFILE PICTURE ────────────────────────────────
                    // Circular image using CircleShape clip modifier
                    // ContentScale.Crop fills the circle without stretching
                    Box(
                        modifier = Modifier
                            .size(100.dp)             // 100x100 circle
                            .clip(CircleShape)         // clips to circle shape
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.icon),
                            contentDescription = "",
                            contentScale = ContentScale.Crop // fills the circle properly
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Change Photo" text button — shows feedback snackbar
                    TextButton(onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Photo picker coming soon!",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }) {
                        Text(
                            text = "Change Photo",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── FULL NAME FIELD ────────────────────────────────
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it }, // updates state on typing
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── EMAIL FIELD ────────────────────────────────────
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email // shows email keyboard
                        ),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── SAVE CHANGES BUTTON ────────────────────────────
                    // Shows success snackbar when tapped
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Profile updated successfully!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = TextOnPrimary
                        )
                    ) {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════════
            // SECTION 2 — CHANGE PASSWORD
            // 3 password fields with eye toggles + match validation
            // ═══════════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                ) {
                    Text(
                        text = "Change Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // ── CURRENT PASSWORD FIELD ─────────────────────────
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        // Show or hide password text based on toggle state
                        visualTransformation = if (currentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                Icon(
                                    imageVector = if (currentPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = IconPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── NEW PASSWORD FIELD ─────────────────────────────
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                Icon(
                                    imageVector = if (newPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = IconPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── CONFIRM NEW PASSWORD FIELD ─────────────────────
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = IconPrimary
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = BorderLight,
                            focusedLabelColor = PrimaryBlue,
                            unfocusedLabelColor = TextSecondary,
                            cursorColor = PrimaryBlue
                        ),
                        // Show red error border if passwords don't match
                        isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                    )

                    // ── PASSWORD MISMATCH ERROR ────────────────────────
                    // Only shown when confirmPassword has text AND passwords don't match
                    if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        Text(
                            text = "Passwords do not match",
                            fontSize = 12.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── UPDATE PASSWORD BUTTON ─────────────────────────
                    // Enabled ONLY when: all 3 fields filled AND new passwords match
                    // Shows success snackbar and clears fields on tap
                    Button(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Password updated successfully!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                            // Clear password fields after successful update
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = TextOnPrimary
                        ),
                        enabled = currentPassword.isNotEmpty() &&
                                 newPassword.isNotEmpty() &&
                                 confirmPassword.isNotEmpty() &&
                                 newPassword == confirmPassword
                    ) {
                        Text("Update Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            // SECTION 3 — APPEARANCE
            // Dark Mode toggle — FULLY FUNCTIONAL
            // Uses ThemeManager singleton to change the app theme globally

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = "Appearance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // ── DARK MODE TOGGLE ───────────────────────────────
                    // ThemeManager.isDarkMode = current value of the dark mode state
                    // ThemeManager.setDarkMode(it) = called when toggle changes
                    // 'it' = the new boolean value passed by the Switch
                    SettingItem(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark Mode",
                        description = "Switch between light and dark theme",
                        isChecked = ThemeManager.isDarkMode, // reads current dark mode state
                        onCheckedChange = {
                            ThemeManager.setDarkMode(it) // updates global theme (triggers full app redraw)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════════
            // SECTION 4 — PREFERENCES
            // Notifications toggle (local state only, no real action yet)
            // ═══════════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = "Preferences",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Notifications toggle — uses local notificationsEnabled state
                    // Shows snackbar feedback when toggled
                    SettingItem(
                        icon = Icons.Filled.Notifications,
                        title = "Notifications",
                        description = "Receive push notifications",
                        isChecked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = if (it) "Notifications enabled" else "Notifications disabled",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ═══════════════════════════════════════════════════════════
            // SECTION 5 — ABOUT
            // Shows app info: version, privacy policy, terms of service
            // ═══════════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = "About",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Version number (not clickable)
                    AboutItem(title = "Version", value = "1.0.0")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Privacy Policy (clickable — shows snackbar)
                    AboutItem(
                        title = "Privacy Policy",
                        value = "View",
                        isClickable = true,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Privacy Policy page coming soon!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Terms of Service (clickable — shows snackbar)
                    AboutItem(
                        title = "Terms of Service",
                        value = "View",
                        isClickable = true,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Terms of Service page coming soon!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ═══════════════════════════════════════════════════════════
            // LOG OUT BUTTON
            // Red-tinted button at the very bottom of the screen.
            // When tapped: shows confirmation dialog before logging out
            // ═══════════════════════════════════════════════════════════
            Button(
                onClick = { showLogoutDialog = true }, // show confirmation dialog
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.1f), // very light red background
                    contentColor = ErrorRed                        // red text and icon
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(0.dp) // no shadow
            ) {
                Icon(Icons.Filled.Logout, contentDescription = "Logout")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp)) // bottom breathing room
        }
    }
}

// ─────────────────────────────────────────────────────────────
// SettingItem — A single toggle setting row.
// Layout: [Icon] [Title + Description]         [Switch]
//
// @param icon             Icon shown on the left
// @param title            Bold setting name (e.g. "Dark Mode")
// @param description      Subtitle below the title
// @param isChecked        Current toggle state (true = ON)
// @param onCheckedChange  Called with new boolean when toggle is tapped
// ─────────────────────────────────────────────────────────────
@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Setting icon on the left
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Title and description text — takes up all space between icon and switch
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Toggle switch on the right — blue when ON, grey when OFF
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange, // passes new value back to caller
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextOnPrimary,    // white thumb when ON
                checkedTrackColor = PrimaryBlue,       // blue track when ON
                uncheckedThumbColor = TextOnPrimary,   // white thumb when OFF
                uncheckedTrackColor = BorderMedium     // grey track when OFF
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────
// AboutItem — A single row in the About section.
// Layout: [Title]                    [Value  >]  (if clickable)
//         [Title]                    [Value]     (if not clickable)
//
// @param title       Label on the left (e.g. "Version")
// @param value       Value on the right (e.g. "1.0.0" or "View")
// @param isClickable Whether the row has a click action and a > arrow
// @param onClick     Action to perform when the clickable item is tapped
// ─────────────────────────────────────────────────────────────
@Composable
fun AboutItem(
    title: String,
    value: String,
    isClickable: Boolean = false, // false by default — non-clickable
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // Only add clickable modifier if isClickable = true
                if (isClickable) Modifier.clickable { onClick() }
                else Modifier
            ),
        horizontalArrangement = Arrangement.SpaceBetween, // pushes left and right apart
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: setting label
        Text(
            text = title,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )

        // Right side: value + optional arrow icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                fontSize = 15.sp,
                // Blue text if clickable ("View"), grey text if not clickable ("1.0.0")
                color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isClickable) FontWeight.Medium else FontWeight.Normal
            )

            // Show a right-arrow ">" icon only if the item is clickable
            if (isClickable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── PREVIEW ───────────────────────────────────────────────────
// Used only by Android Studio's preview panel — does NOT run on device
@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    NoteHubTheme {
        SettingsScreen(onLogout = {})
    }
}
