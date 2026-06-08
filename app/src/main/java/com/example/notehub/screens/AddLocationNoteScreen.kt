package com.example.notehub.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Lock
import com.example.notehub.ui.security.BiometricHelper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.theme.*
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

/**
 * AddLocationNoteScreen — Note creation form integrated with GPS coordinates capturing,
 * Geocoder reverse-geocoding, note category chips, palette selections, and a live map preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLocationNoteScreen(
    onNavigateBack: () -> Unit,
    viewModel: LocationNotesViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var selectedColor by remember { mutableStateOf(PrimaryBlue) }
    var isSecured by remember { mutableStateOf(false) }
    var securityPassword by remember { mutableStateOf("") }

    // LatLng state
    var noteLatitude by remember { mutableStateOf(0.0) }
    var noteLongitude by remember { mutableStateOf(0.0) }
    var resolvedAddress by remember { mutableStateOf("") }
    var locationFetched by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Work", "Study", "Travel")
    val noteColors = listOf(PrimaryBlue, SuccessGreen, WarningYellow, InfoBlue, AccentPurple, ErrorRed)

    // Maps camera controller
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 2f)
    }

    // Update map preview camera when location is captured
    LaunchedEffect(noteLatitude, noteLongitude) {
        if (locationFetched) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                LatLng(noteLatitude, noteLongitude),
                16f
            )
        }
    }

    // Runtime location permission checker
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            viewModel.requestCurrentGPSLocation { lat, lng, addr ->
                noteLatitude = lat
                noteLongitude = lng
                resolvedAddress = addr
                locationFetched = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Location Note", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank() && locationFetched) {
                                // Convert selected color object to hex string representation
                                val hexString = String.format("#%06X", 0xFFFFFF and selectedColor.value.toLong().toInt())
                                val pass = if (isSecured && securityPassword.isNotBlank()) securityPassword else null
                                viewModel.saveLocationNote(
                                    title = title,
                                    description = description,
                                    latitude = noteLatitude,
                                    longitude = noteLongitude,
                                    address = resolvedAddress,
                                    category = selectedCategory,
                                    colorHex = hexString,
                                    isSecured = isSecured,
                                    securityPassword = pass,
                                    onSuccess = onNavigateBack
                                )
                            }
                        },
                        enabled = title.isNotBlank() && description.isNotBlank() && (!isSecured || securityPassword.isNotBlank()) && locationFetched && !viewModel.isFetchingLocationDetails
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (title.isNotBlank() && description.isNotBlank() && locationFetched) PrimaryBlue else TextTertiary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Form Fields: Title & Content
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = BorderMedium,
                    focusedLabelColor = selectedColor
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = BorderMedium,
                    focusedLabelColor = selectedColor
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Category Selection
            Text(
                text = "Category",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedColor.copy(alpha = 0.1f),
                            selectedLabelColor = selectedColor,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategory == category) selectedColor else BorderMedium,
                            selectedBorderColor = selectedColor,
                            enabled = true,
                            selected = selectedCategory == category
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note Color Picker
            Text(
                text = "Note Color Marker",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                noteColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == color) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── SECURE NOTE TOGGLE ─────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (isSecured) PrimaryBlue else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Secure Note (Biometrics/Password)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isSecured,
                    onCheckedChange = { isSecured = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundWhite,
                        checkedTrackColor = PrimaryBlue
                    )
                )
            }

            if (isSecured) {
                val isBiometricsSupported = remember { BiometricHelper.isBiometricAvailable(context) }
                if (isBiometricsSupported) {
                    Text(
                        text = "✓ Device biometrics (fingerprint/face lock) detected. This note will require your fingerprint to open.",
                        fontSize = 13.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        text = "⚠ No registered biometrics found on this device. Only the backup password below will be used to unlock.",
                        fontSize = 13.sp,
                        color = WarningYellow,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                OutlinedTextField(
                    value = securityPassword,
                    onValueChange = { securityPassword = it },
                    label = { Text("Backup Security Password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = BorderMedium,
                        focusedLabelColor = selectedColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GPS Location Capturing Module
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewModel.isFetchingLocationDetails) {
                        CircularProgressIndicator(
                            color = selectedColor,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Retrieving GPS details & reverse-geocoding...",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    } else if (locationFetched) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Place,
                                contentDescription = "Location Pin",
                                tint = selectedColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = resolvedAddress,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Lat: ${String.format("%.5f", noteLatitude)}, Lng: ${String.format("%.5f", noteLongitude)}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        // Map Preview Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BorderLight)
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(
                                    mapStyleOptions = if (ThemeManager.isDarkMode) MapStyleOptions(googleMapDarkStyle) else null
                                ),
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = false,
                                    scrollGesturesEnabled = false,
                                    zoomGesturesEnabled = false,
                                    tiltGesturesEnabled = false,
                                    rotationGesturesEnabled = false
                                )
                            ) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(noteLatitude, noteLongitude)),
                                    title = "Selected Spot"
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Location Pinned Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Fetch your active GPS coordinates to attach a location to this note.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (hasLocationPermission) {
                                viewModel.requestCurrentGPSLocation { lat, lng, addr ->
                                    noteLatitude = lat
                                    noteLongitude = lng
                                    resolvedAddress = addr
                                    locationFetched = true
                                }
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.GpsFixed,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (locationFetched) "Update GPS Location" else "Pin Current GPS Location",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun AddLocationNoteScreenPreview() {
    com.example.notehub.ui.theme.NoteHubTheme {
        AddLocationNoteScreen(onNavigateBack = {})
    }
}
