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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
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
import com.example.notehub.data.Note
import com.example.notehub.data.SampleData
import com.example.notehub.ui.theme.*
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    noteId: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: LocationNotesViewModel = viewModel()
) {
    val existingNote = remember(noteId) {
        if (noteId != null) viewModel.notes.find { it.id == noteId } else null
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var content by remember { mutableStateOf(existingNote?.description ?: "") }
    var selectedColor by remember { mutableStateOf(existingNote?.let { parseHexColor(it.colorHex) } ?: PrimaryBlue) }
    var selectedCategory by remember { mutableStateOf(existingNote?.category ?: "Personal") }
    var isPinned by remember { mutableStateOf(false) }
    var isSecured by remember { mutableStateOf(existingNote?.isSecured ?: false) }
    var securityPassword by remember { mutableStateOf(existingNote?.securityPassword ?: "") }

    // Location specific states
    var isLocationBased by remember { mutableStateOf(existingNote?.let { it.latitude != 0.0 || it.longitude != 0.0 } ?: false) }
    var noteLatitude by remember { mutableStateOf(existingNote?.latitude ?: 0.0) }
    var noteLongitude by remember { mutableStateOf(existingNote?.longitude ?: 0.0) }
    var resolvedAddress by remember { mutableStateOf(existingNote?.address ?: "") }
    var locationFetched by remember { mutableStateOf(existingNote?.let { it.latitude != 0.0 || it.longitude != 0.0 } ?: false) }

    val categories = listOf("Personal", "Work", "Study", "Other")

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

    val context = LocalContext.current
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
        } else {
            isLocationBased = false
        }
    }

    // Automatically request location when toggled ON
    LaunchedEffect(isLocationBased) {
        if (isLocationBased) {
            if (!hasLocationPermission) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else if (!locationFetched) {
                viewModel.requestCurrentGPSLocation { lat, lng, addr ->
                    noteLatitude = lat
                    noteLongitude = lng
                    resolvedAddress = addr
                    locationFetched = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == null) "Add Note" else "Edit Note", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                val hexString = String.format("#%06X", 0xFFFFFF and selectedColor.value.toLong().toInt())
                                val pass = if (isSecured && securityPassword.isNotBlank()) securityPassword else null
                                val lat = if (isLocationBased && locationFetched) noteLatitude else 0.0
                                val lng = if (isLocationBased && locationFetched) noteLongitude else 0.0
                                val addr = if (isLocationBased && locationFetched) resolvedAddress else ""

                                if (noteId == null) {
                                    viewModel.saveLocationNote(
                                        title = title,
                                        description = content,
                                        latitude = lat,
                                        longitude = lng,
                                        address = addr,
                                        category = selectedCategory,
                                        colorHex = hexString,
                                        isSecured = isSecured,
                                        securityPassword = pass,
                                        onSuccess = onNavigateBack
                                    )
                                } else {
                                    viewModel.updateLocationNote(
                                        id = noteId,
                                        title = title,
                                        description = content,
                                        latitude = lat,
                                        longitude = lng,
                                        address = addr,
                                        category = selectedCategory,
                                        colorHex = hexString,
                                        isSecured = isSecured,
                                        securityPassword = pass,
                                        onSuccess = onNavigateBack
                                    )
                                }
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank() && (!isSecured || securityPassword.isNotBlank()) && (!isLocationBased || (locationFetched && !viewModel.isFetchingLocationDetails))
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (title.isNotBlank() && content.isNotBlank() && (!isLocationBased || locationFetched)) PrimaryBlue else TextTertiary
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Category",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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

            // ── COLOR PICKER ROW ───────────────────────────────────────
            Text(
                text = "Note Color",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val noteColors = listOf(PrimaryBlue, SuccessGreen, WarningYellow, InfoBlue, AccentPurple, ErrorRed)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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

            // ── LOCATION TOGGLE ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = if (isLocationBased) PrimaryBlue else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Location-based Note",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isLocationBased,
                    onCheckedChange = { isLocationBased = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundWhite,
                        checkedTrackColor = PrimaryBlue
                    )
                )
            }

            // ── LOCATION DETAILS & MAP PREVIEW ─────────────────────────
            if (isLocationBased) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (viewModel.isFetchingLocationDetails) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Fetching GPS location...",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    } else if (locationFetched) {
                        Text(
                            text = "Address:",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = resolvedAddress.ifEmpty { "Lat: $noteLatitude, Lng: $noteLongitude" },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .border(1.dp, BorderLight.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            val mapProperties = MapProperties(
                                isMyLocationEnabled = true,
                                mapStyleOptions = if (ThemeManager.isDarkMode) MapStyleOptions(googleMapDarkStyle) else null
                            )
                            val mapUiSettings = MapUiSettings(
                                zoomControlsEnabled = false,
                                myLocationButtonEnabled = false,
                                scrollGesturesEnabled = false,
                                zoomGesturesEnabled = false,
                                tiltGesturesEnabled = false,
                                rotationGesturesEnabled = false
                            )
                            
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = mapProperties,
                                uiSettings = mapUiSettings
                            ) {
                                Marker(
                                    state = rememberMarkerState(position = LatLng(noteLatitude, noteLongitude)),
                                    title = "Note Location",
                                    snippet = resolvedAddress
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Failed to fetch GPS location. Ensure location services are enabled.",
                            fontSize = 13.sp,
                            color = ErrorRed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }

            // ── PIN TO TOP TOGGLE ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) WarningYellow else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pin to top",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isPinned,
                    onCheckedChange = { isPinned = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundWhite,
                        checkedTrackColor = PrimaryBlue
                    )
                )
            }

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
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = BorderMedium,
                    focusedLabelColor = selectedColor
                ),
                shape = RoundedCornerShape(12.dp),
                minLines = 5
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNoteScreenPreview() {
    NoteHubTheme {
        AddNoteScreen(onNavigateBack = {})
    }
}
