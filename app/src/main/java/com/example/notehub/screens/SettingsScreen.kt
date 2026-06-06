package com.example.notehub.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.notehub.R
import com.example.notehub.data.AuthService
import com.example.notehub.data.remote.TokenManager
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import java.io.File
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.notehub.utils.NetworkMonitor
import com.example.notehub.ui.security.BiometricHelper

// Helper — creates a temp file in cache/camera_photos/ for the camera to write to
private fun createImageFile(context: Context): File {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    return File.createTempFile("profile_", ".jpg", dir)
}

// Helper — converts the temp File into a content:// URI via FileProvider
private fun getUriForFile(context: Context, file: File): Uri =
    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

/**
 * SettingsScreen — full settings and profile management screen.
 *
 * @param onLogout  Called when the user taps "Log Out" — navigates back to LoginScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // ── STATE VARIABLES ────────────────────────────────────────────

    val storedName = TokenManager.getLoggedInName()
    val storedEmail = TokenManager.getLoggedInEmail()

    // Profile fields (pre-filled with current user data)
    var fullName by remember(storedName, storedEmail) {
        mutableStateOf(if (!storedName.isNullOrEmpty()) storedName else "Arosha")
    }
    var email by remember(storedName, storedEmail) {
        mutableStateOf(if (!storedEmail.isNullOrEmpty()) storedEmail else "Arosha@gmail.com")
    }

    // Profile photo URI — null = show default icon drawable
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Holds the temp file URI used when taking a photo with the camera
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    // Controls whether the "Camera or Gallery?" bottom sheet is visible
    var showPhotoPickerSheet by remember { mutableStateOf(false) }

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

    // Battery diagnostic state
    var batteryPercentage by remember { mutableStateOf(100) }
    var isBatteryCharging by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryPercentage = (level * 100) / scale
                    }
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    isBatteryCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                        status == BatteryManager.BATTERY_STATUS_FULL
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // ── ACTIVITY RESULT LAUNCHERS ──────────────────────────────────

    // 1. Gallery / Photo Picker — opens the system photo picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profilePhotoUri = it }
    }

    // 2. Camera — takes a photo and saves it to cameraImageUri
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            // TakePicture wrote the photo to cameraImageUri — assign it to show it
            profilePhotoUri = cameraImageUri
        }
    }

    // 3. Camera permission — asks for CAMERA permission before launching camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            // Permission granted — create temp file and launch camera
            val tempFile = createImageFile(context)
            val uri = getUriForFile(context, tempFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Camera permission is required to take a photo.",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

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
                        // Clear JWT token + user identity so next login starts fresh
                        AuthService.logout()
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
                    // Tapping the photo or the camera badge opens the picker sheet
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clickable { showPhotoPickerSheet = true },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        // Circular avatar — shows selected photo or default icon
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .border(3.dp, PrimaryBlue.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                // Show the selected/taken photo using Coil's AsyncImage
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Fallback: show the default app icon
                                androidx.compose.foundation.Image(
                                    painter = painterResource(R.drawable.icon),
                                    contentDescription = "Default Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        // Camera badge overlaid on bottom-right of avatar
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // "Change Photo" text button — opens the picker sheet
                    TextButton(onClick = { showPhotoPickerSheet = true }) {
                        Text(
                            text = "Change Photo",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // ── PHOTO SOURCE PICKER BOTTOM SHEET ───────────────
                    if (showPhotoPickerSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showPhotoPickerSheet = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(bottom = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Choose Photo Source",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                                Spacer(modifier = Modifier.height(16.dp))

                                // ── CAMERA OPTION ────────────────────
                                PhotoSourceOption(
                                    icon = Icons.Filled.PhotoCamera,
                                    label = "Take a Photo",
                                    description = "Use your device camera",
                                    color = PrimaryBlue,
                                    onClick = {
                                        showPhotoPickerSheet = false
                                        // Request camera permission (handles both granted and denied)
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // ── GALLERY OPTION ───────────────────
                                PhotoSourceOption(
                                    icon = Icons.Filled.PhotoLibrary,
                                    label = "Choose from Gallery",
                                    description = "Pick an existing photo",
                                    color = SuccessGreen,
                                    onClick = {
                                        showPhotoPickerSheet = false
                                        galleryLauncher.launch("image/*")
                                    }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // ── REMOVE PHOTO OPTION ──────────────
                                if (profilePhotoUri != null) {
                                    PhotoSourceOption(
                                        icon = Icons.Filled.Delete,
                                        label = "Remove Photo",
                                        description = "Revert to default avatar",
                                        color = ErrorRed,
                                        onClick = {
                                            showPhotoPickerSheet = false
                                            profilePhotoUri = null
                                        }
                                    )
                                }
                            }
                        }
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
                            TokenManager.saveUser(email, fullName, TokenManager.getLoggedInUserId())
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

                    val systemInDark = isSystemInDarkTheme()
                    val activeDarkMode = remember(systemInDark, ThemeManager.hasUserSetDarkModeManualPref, ThemeManager.isDarkMode) {
                        if (ThemeManager.hasUserSetDarkModeManualPref) ThemeManager.isDarkMode else systemInDark
                    }
                    SettingItem(
                        icon = Icons.Filled.DarkMode,
                        title = "Dark Mode",
                        description = if (ThemeManager.hasUserSetDarkModeManualPref) "Manually overridden" else "Following system theme",
                        isChecked = activeDarkMode,
                        onCheckedChange = {
                            ThemeManager.setDarkMode(it)
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
            // SECTION 4.5 — DEVICE STATUS DIAGNOSTICS
            // ═══════════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text(
                        text = "Device Status",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // 1. Network Connectivity Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (NetworkMonitor.isOnline()) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                                contentDescription = "Network Status",
                                tint = if (NetworkMonitor.isOnline()) PrimaryBlue else ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Network Connectivity", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    if (NetworkMonitor.isOnline()) "AWS Backend Connected" else "Offline Mode Active",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (NetworkMonitor.isOnline()) "Online" else "Offline",
                            color = if (NetworkMonitor.isOnline()) SuccessGreen else Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Battery Sensor Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Battery Status",
                                tint = if (batteryPercentage > 20) SuccessGreen else ErrorRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Battery Status", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    if (isBatteryCharging) "Charging" else "Discharging",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "$batteryPercentage%",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Biometric Sensor Availability
                    val isBiometricsSupported = remember { BiometricHelper.isBiometricAvailable(context) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Fingerprint,
                                contentDescription = "Biometric Sensor",
                                tint = if (isBiometricsSupported) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Biometric Authentication", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    if (isBiometricsSupported) "Fingerprint / Face ID ready" else "Biometrics not supported on device",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = if (isBiometricsSupported) "Available" else "Disabled",
                            color = if (isBiometricsSupported) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
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

// ─────────────────────────────────────────────────────────────
// PhotoSourceOption — A clickable row shown in the photo picker sheet.
// Layout: [Colored Icon Box]  [Label + Description]  [Chevron]
//
// @param icon         Icon to display (Camera, Gallery, or Delete)
// @param label        Main action text (e.g. "Take a Photo")
// @param description  Subtitle (e.g. "Use your device camera")
// @param color        Accent colour for the icon box background
// @param onClick      Action to perform when the row is tapped
// ─────────────────────────────────────────────────────────────
@Composable
fun PhotoSourceOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Coloured icon box
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Label and description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
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

            // Chevron arrow
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
