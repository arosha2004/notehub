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
import com.example.notehub.data.AuthResult
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

// Helper — copies the photo URI (gallery or camera) to a permanent internal files location
private fun saveProfilePhoto(context: Context, userId: Int, sourceUri: Uri?): String? {
    val dir = File(context.filesDir, "profile_photos").also { it.mkdirs() }
    val destFile = File(dir, "profile_$userId.jpg")
    
    if (sourceUri == null) {
        if (destFile.exists()) {
            destFile.delete()
        }
        return null
    }
    
    val destUri = Uri.fromFile(destFile)
    if (sourceUri == destUri) {
        return destUri.toString()
    }
    
    return try {
        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            destFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        destUri.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Full settings and profile management screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    // STATE VARIABLES

    val storedName = TokenManager.getLoggedInName()
    val storedEmail = TokenManager.getLoggedInEmail()

    var fullName by remember(storedName, storedEmail) {
        mutableStateOf(if (!storedName.isNullOrEmpty()) storedName else "Arosha")
    }
    var email by remember(storedName, storedEmail) {
        mutableStateOf(if (!storedEmail.isNullOrEmpty()) storedEmail else "Arosha@gmail.com")
    }

    val userId = TokenManager.getLoggedInUserId()
    val storedPhotoUriString = remember(userId) { TokenManager.getLoggedInProfilePhoto(userId) }

    var profilePhotoUri by remember(userId, storedPhotoUriString) {
        mutableStateOf(storedPhotoUriString?.let { Uri.parse(it) })
    }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPhotoPickerSheet by remember { mutableStateOf(false) }



    var notificationsEnabled by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

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

    // ACTIVITY RESULT LAUNCHERS

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { profilePhotoUri = it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            profilePhotoUri = cameraImageUri
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val tempFile = createImageFile(context)
            val uri = getUriForFile(context, tempFile)
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Camera permission is required.")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = { Icon(Icons.Filled.Logout, contentDescription = null, tint = ErrorRed) },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        AuthService.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = TextOnPrimary)
                ) { Text("Log Out") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
            text = { 
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Welcome to NoteHub! Your privacy is very important to us.", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    Text("1. Data Collection\nWe securely store the notes and profile information you provide. Location data is only used when you explicitly attach it to a note.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. Security\nAll your notes are transmitted securely. Notes marked as 'Secure' are encrypted on your device and require biometric authentication to unlock.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3. Third Parties\nWe do not sell your personal data to any third parties.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4. Offline Mode\nWhile offline, your notes are cached locally on your device in a secure format.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("By using this app, you agree to this privacy policy.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) { Text("Close", color = Color.White) }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // SECTION 1 — PROFILE SETTINGS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Profile Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clickable { showPhotoPickerSheet = true },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .border(3.dp, PrimaryBlue.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (profilePhotoUri != null) {
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = "Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(R.drawable.icon),
                                    contentDescription = "Default Profile Photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(80.dp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    TextButton(onClick = { showPhotoPickerSheet = true }) {
                        Text("Change Photo", color = MaterialTheme.colorScheme.primary)
                    }

                    if (showPhotoPickerSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showPhotoPickerSheet = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Choose Photo Source", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))
                                PhotoSourceOption(Icons.Filled.PhotoCamera, "Take a Photo", "Use camera", PrimaryBlue) {
                                    showPhotoPickerSheet = false
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                PhotoSourceOption(Icons.Filled.PhotoLibrary, "Choose from Gallery", "Pick a photo", SuccessGreen) {
                                    showPhotoPickerSheet = false
                                    galleryLauncher.launch("image/*")
                                }
                                if (profilePhotoUri != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    PhotoSourceOption(Icons.Filled.Delete, "Remove Photo", "Revert to default", ErrorRed) {
                                        showPhotoPickerSheet = false
                                        profilePhotoUri = null
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val currentUserId = TokenManager.getLoggedInUserId()
                            val savedPhotoUriStr = saveProfilePhoto(context, currentUserId, profilePhotoUri)
                            TokenManager.saveProfilePhoto(currentUserId, savedPhotoUriStr)
                            profilePhotoUri = savedPhotoUriStr?.let { Uri.parse(it) }
                            TokenManager.saveUser(email, fullName, currentUserId)
                            scope.launch {
                                snackbarHostState.showSnackbar("Profile updated successfully!")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))



            // SECTION 3 — APPEARANCE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Appearance", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
                    val systemInDark = isSystemInDarkTheme()
                    val activeDarkMode = remember(systemInDark, ThemeManager.hasUserSetDarkModeManualPref, ThemeManager.isDarkMode) {
                        if (ThemeManager.hasUserSetDarkModeManualPref) ThemeManager.isDarkMode else systemInDark
                    }
                    SettingItem(Icons.Filled.DarkMode, "Dark Mode", if (ThemeManager.hasUserSetDarkModeManualPref) "Manual override" else "Following system", activeDarkMode) {
                        ThemeManager.setDarkMode(it)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4 — PREFERENCES
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Preferences", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
                    SettingItem(Icons.Filled.Notifications, "Notifications", "Receive push notifications", notificationsEnabled) {
                        notificationsEnabled = it
                        scope.launch { snackbarHostState.showSnackbar(if (it) "Enabled" else "Disabled") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 4.5 — DEVICE STATUS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("Device Status", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
                    
                    // Network
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = if (NetworkMonitor.isOnline()) Icons.Filled.Wifi else Icons.Filled.WifiOff, contentDescription = null, tint = if (NetworkMonitor.isOnline()) PrimaryBlue else ErrorRed)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Network", fontWeight = FontWeight.SemiBold)
                                Text(if (NetworkMonitor.isOnline()) "AWS Backend Connected" else "Offline", fontSize = 12.sp)
                            }
                        }
                        Text(if (NetworkMonitor.isOnline()) "Online" else "Offline", color = if (NetworkMonitor.isOnline()) SuccessGreen else Color(0xFFF59E0B), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Battery
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, contentDescription = null, tint = if (batteryPercentage > 20) SuccessGreen else ErrorRed)
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Battery", fontWeight = FontWeight.SemiBold)
                                Text(if (isBatteryCharging) "Charging" else "Discharging", fontSize = 12.sp)
                            }
                        }
                        Text("$batteryPercentage%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 5 — ABOUT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Text("About", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 20.dp))
                    AboutItem("Version", "1.0.0")
                    Spacer(modifier = Modifier.height(16.dp))
                    AboutItem("Privacy Policy", "View", true) {
                        showPrivacyPolicyDialog = true
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.1f), contentColor = ErrorRed),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = isChecked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = PrimaryBlue))
    }
}

@Composable
fun AboutItem(title: String, value: String, isClickable: Boolean = false, onClick: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().then(if (isClickable) Modifier.clickable { onClick() } else Modifier), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = value, fontSize = 15.sp, color = if (isClickable) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant)
            if (isClickable) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun PhotoSourceOption(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, description: String, color: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), color = color.copy(alpha = 0.08f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(text = description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun SettingsScreenPreview() {
    com.example.notehub.ui.theme.NoteHubTheme {
        SettingsScreen(onLogout = {})
    }
}
