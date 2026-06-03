package com.example.notehub.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.theme.*
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*

/**
 * LocationNoteDetailScreen — Displays note information (title, description, category, and date),
 * rendering a centered Google Map and providing quick launch actions to open maps routing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationNoteDetailScreen(
    noteId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit = {},
    viewModel: LocationNotesViewModel = viewModel()
) {
    val context = LocalContext.current
    val note = viewModel.notes.find { it.id == noteId }
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (note == null) {
        // Fallback if note details are not found
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Note Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryBlue)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading note details...", color = TextSecondary)
                }
            }
        }
        return
    }

    val noteColor = parseHexColor(note.colorHex)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(note.latitude, note.longitude), 15f)
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this location note? This will also remove its geofence reminders.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteLocationNote(note.id, onSuccess = onNavigateBack)
                    }
                ) {
                    Text("Delete", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEditClick(note.id) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Location Note",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete Location Note",
                            tint = ErrorRed
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
            // Note Details Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Tag
                        Box(
                            modifier = Modifier
                                .background(noteColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = note.category.uppercase(),
                                color = noteColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Created Date
                        Text(
                            text = note.date,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = note.description,
                        fontSize = 15.sp,
                        color = TextSecondary,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Map and Navigation Routing Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderLight.copy(alpha = 0.15f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = "Address",
                            tint = noteColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.address,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Lat: ${String.format("%.5f", note.latitude)}, Lng: ${String.format("%.5f", note.longitude)}",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Map Container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
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
                                zoomControlsEnabled = false
                            )
                        ) {
                            Marker(
                                state = rememberMarkerState(position = LatLng(note.latitude, note.longitude)),
                                title = note.title,
                                snippet = note.address
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // "Get Directions" trigger
                    Button(
                        onClick = {
                            val uri = Uri.parse("geo:${note.latitude},${note.longitude}?q=${note.latitude},${note.longitude}(${Uri.encode(note.title)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            // Fallback if official Google Maps app is not installed
                            if (mapIntent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(mapIntent)
                            } else {
                                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                                context.startActivity(fallbackIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = noteColor,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Directions / Navigation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
