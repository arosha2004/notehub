package com.example.notehub.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.notehub.ui.security.BiometricHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.domain.model.LocationNote
import com.example.notehub.ui.theme.*
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

/**
 * LocationNotesScreen — Fullscreen Map display for location-based notes.
 * Integrates Google Maps, dynamic live searches, radius proximity filter,
 * custom marker details, and high fidelity Light/Dark theme styles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationNotesScreen(
    onNavigateBack: () -> Unit,
    onAddNoteClick: () -> Unit,
    onNoteClick: (Int) -> Unit,
    viewModel: LocationNotesViewModel = viewModel()
) {
    val context = LocalContext.current
    
    // Security auth states
    var activeAuthNoteId by remember { mutableStateOf<Int?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Launcher to request runtime permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.values.any { it }
        if (hasLocationPermission) {
            viewModel.fetchNotes()
            viewModel.requestCurrentGPSLocation()
        }
    }

    // Trigger permission request on start if not granted
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.requestCurrentGPSLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Location Notes", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = onAddNoteClick,
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Location Note", modifier = Modifier.size(26.dp))
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasLocationPermission) {
                // Main Map Screen Layout
                MapContent(
                    viewModel = viewModel,
                    onNoteClick = { noteId ->
                        val originalNote = viewModel.notes.find { it.id == noteId }
                        if (originalNote?.isSecured == true) {
                            activeAuthNoteId = noteId
                            passwordInput = ""
                            passwordError = false
                            val activity = context as? androidx.fragment.app.FragmentActivity
                            if (activity != null && BiometricHelper.isBiometricAvailable(activity)) {
                                BiometricHelper.showBiometricPrompt(
                                    activity = activity,
                                    onSuccess = {
                                        onNoteClick(noteId)
                                    },
                                    onError = { error ->
                                        showPasswordDialog = true
                                    }
                                )
                            } else {
                                showPasswordDialog = true
                            }
                        } else {
                            onNoteClick(noteId)
                        }
                    }
                )
            } else {
                // Permission Fallback Screen
                PermissionRequestFallback {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            if (showPasswordDialog && activeAuthNoteId != null) {
                val noteToAuth = viewModel.notes.find { it.id == activeAuthNoteId }
                AlertDialog(
                    onDismissRequest = {
                        showPasswordDialog = false
                        activeAuthNoteId = null
                    },
                    title = { Text("Enter Note Password", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text("This note is protected. Enter password to unlock.", fontSize = 14.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    passwordError = false
                                },
                                label = { Text("Password") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = passwordError,
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            if (passwordError) {
                                Text("Incorrect password. Try again.", color = ErrorRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (passwordInput == noteToAuth?.securityPassword) {
                                    showPasswordDialog = false
                                    onNoteClick(activeAuthNoteId!!)
                                    activeAuthNoteId = null
                                } else {
                                    passwordError = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Text("Unlock", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showPasswordDialog = false
                                activeAuthNoteId = null
                            }
                        ) {
                            Text("Cancel", color = TextSecondary)
                        }
                    },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

/**
 * MapContent — Coordinates map rendering, overlaying widgets, and sliders.
 */
@Composable
fun MapContent(
    viewModel: LocationNotesViewModel,
    onNoteClick: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    val filteredNotes = viewModel.getFilteredNotesList()

    // Default map center (San Francisco)
    val defaultLatLng = LatLng(37.7749, -122.4194)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 12f)
    }

    // Center camera on user location when acquired
    val userLoc = viewModel.userLocation
    LaunchedEffect(userLoc) {
        if (userLoc != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(userLoc.latitude, userLoc.longitude),
                    14f
                )
            )
        }
    }

    // Google Map styling options (supports custom dark theme)
    val context = LocalContext.current
    val mapProperties = MapProperties(
        isMyLocationEnabled = true,
        mapStyleOptions = if (ThemeManager.isDarkMode) {
            MapStyleOptions(googleMapDarkStyle)
        } else {
            null
        }
    )
    val mapUiSettings = MapUiSettings(
        myLocationButtonEnabled = true,
        zoomControlsEnabled = false
    )

    // Render Google Map
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        onMapClick = {
            // Dismiss details card on map clicks
            viewModel.selectedNote = null
        }
    ) {
        // Draw markers for all filtered notes
        filteredNotes.forEach { note ->
            Marker(
                state = rememberMarkerState(position = LatLng(note.latitude, note.longitude)),
                title = if (note.isSecured) "🔒 Secure Note" else note.title,
                snippet = if (note.isSecured) "Content is locked" else note.address,
                onClick = {
                    viewModel.selectedNote = note
                    scope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(note.latitude, note.longitude),
                                15f
                            )
                        )
                    }
                    true // Intercept click
                }
            )
        }
    }

    // Floating Search bar & Filter controls
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Search Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("Search location notes...", color = TextTertiary) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )
                if (viewModel.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.searchQuery = "" }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear search", tint = TextSecondary)
                    }
                }
                
                // Toggle distance filter button
                IconButton(
                    onClick = { viewModel.isRadiusFilterEnabled = !viewModel.isRadiusFilterEnabled },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (viewModel.isRadiusFilterEnabled) PrimaryBlue.copy(alpha = 0.15f) else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = "Proximity filter",
                        tint = if (viewModel.isRadiusFilterEnabled) PrimaryBlue else TextSecondary
                    )
                }
            }
        }

        // Proximity Slider Panel
        AnimatedVisibility(
            visible = viewModel.isRadiusFilterEnabled,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby Notes Radius Filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${viewModel.radiusFilter.toInt()} km",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )
                    }
                    Slider(
                        value = viewModel.radiusFilter,
                        onValueChange = { viewModel.radiusFilter = it },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryBlue,
                            activeTrackColor = PrimaryBlue,
                            inactiveTrackColor = BorderMedium
                        )
                    )
                }
            }
        }
    }

    // Overlaid Card / List overlays
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedContent(
            targetState = viewModel.selectedNote,
            transitionSpec = {
                slideInVertically(initialOffsetY = { it }) + fadeIn() togetherWith
                slideOutVertically(targetOffsetY = { it }) + fadeOut()
            },
            label = "NoteOverlay"
        ) { note ->
            if (note != null) {
                // Focus note summary card
                LocationNotePreviewCard(
                    note = note,
                    distanceText = viewModel.getFormattedDistance(note),
                    onDetailsClick = { onNoteClick(note.id) },
                    onCloseClick = { viewModel.selectedNote = null }
                )
            } else {
                // Horizontal list of nearby notes
                if (filteredNotes.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(filteredNotes) { item ->
                            LocationNoteMiniCard(
                                note = item,
                                distanceText = viewModel.getFormattedDistance(item),
                                onClick = {
                                    viewModel.selectedNote = item
                                    scope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(item.latitude, item.longitude),
                                                15f
                                            )
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * LocationNotePreviewCard — Displays expanded preview when a marker is tapped.
 */
@Composable
fun LocationNotePreviewCard(
    note: LocationNote,
    distanceText: String,
    onDetailsClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val categoryColor = parseHexColor(note.colorHex)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, BorderLight.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip
                Box(
                    modifier = Modifier
                        .background(categoryColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = note.category.uppercase(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = distanceText,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onCloseClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val displayTitle = if (note.isSecured) "🔒 Secure Note" else note.title
            val displayDescription = if (note.isSecured) "Content is locked. Tap details to unlock." else note.description

            Text(
                text = displayTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = displayDescription,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = note.address,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDetailsClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Open Details", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }

                Button(
                    onClick = {
                        val directionsUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${note.latitude},${note.longitude}")
                        try {
                            val mapIntent = Intent(Intent.ACTION_VIEW, directionsUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            context.startActivity(mapIntent)
                        } catch (e: ActivityNotFoundException) {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, directionsUri)
                            context.startActivity(fallbackIntent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Directions", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                }
            }
        }
    }
}

/**
 * LocationNoteMiniCard — Single card template inside the horizontal lazy row.
 */
@Composable
fun LocationNoteMiniCard(
    note: LocationNote,
    distanceText: String,
    onClick: () -> Unit
) {
    val categoryColor = parseHexColor(note.colorHex)

    Card(
        modifier = Modifier
            .width(260.dp)
            .height(135.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = distanceText,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val displayTitle = if (note.isSecured) "🔒 Secure Note" else note.title
                val displayDescription = if (note.isSecured) "Content is locked. Tap to authenticate." else note.description

                Text(
                    text = displayTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = displayDescription,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = note.address,
                    fontSize = 11.sp,
                    color = TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * PermissionRequestFallback — UI shown when permissions are not granted yet.
 */
@Composable
fun PermissionRequestFallback(
    onRequestPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Location Pin",
                tint = PrimaryBlue,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enable Location Access",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "To visualize location-based notes on Google Maps, find nearby notes, and receive geofence reminders when traveling near notes, NoteHub requires location permissions.",
            fontSize = 14.sp,
            color = TextSecondary,
            lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onRequestPermissions,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Grant Location Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/**
 * Parses hex color strings safely into Color objects.
 */
fun parseHexColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        PrimaryBlue
    }
}

/**
 * Custom dark style for Google Maps (JSON layout).
 */
val googleMapDarkStyle = """
[
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#1e1b2e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#8ec3b9"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#1a1827"
      }
    ]
  },
  {
    "featureType": "administrative",
    "elementType": "geometry",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2d2a3d"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d1d5db"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2d2a3d"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f1c2c"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca3af"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2d2a3d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#0d0a1b"
      }
    ]
  }
]
""".trimIndent()


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun LocationNotesScreenPreview() {
    com.example.notehub.ui.theme.NoteHubTheme {
        LocationNotesScreen(onNavigateBack = {}, onAddNoteClick = {}, onNoteClick = {})
    }
}
