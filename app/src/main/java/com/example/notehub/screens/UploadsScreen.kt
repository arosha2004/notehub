package com.example.notehub.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

data class Upload(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val uploadDate: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * UploadsScreen — Shows the list of uploaded files.
 */
@Composable
fun UploadsScreen() {
    // Mutable list so uploads can be added/removed dynamically
    val uploads = remember {
        mutableStateListOf<Upload>()
    }

    // State for filter chip selection
    var selectedFilter by remember { mutableStateOf("All Files") }
    val filterOptions = listOf("All Files", "Images", "Documents")

    // Snackbar host for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Scaffold provides the screen layout + floating action button placement
    Scaffold(
        // ── FLOATING ACTION BUTTON ─────────────────────────────────
        // Blue upload (⬆) button in the bottom right corner
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Add a sample upload to demonstrate the feature
                    val newId = (uploads.maxOfOrNull { it.id } ?: 0) + 1
                    val isImage = (1..2).random() == 1
                    uploads.add(
                        Upload(
                            id = newId,
                            fileName = if (isImage) "Photo_$newId.jpg" else "Document_$newId.pdf",
                            fileType = if (isImage) "JPG" else "PDF",
                            fileSize = "${(100..5000).random()} KB",
                            uploadDate = "May 7, 2026",
                            icon = if (isImage) Icons.Filled.Image else Icons.Filled.Description
                        )
                    )
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "File uploaded successfully!",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Upload,
                    contentDescription = "Upload File"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)                // avoid FAB overlap
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // BLUE HEADER CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PrimaryBlue
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Uploads",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextOnPrimary
                        )
                        Text(
                            text = "${uploads.size} files",
                            fontSize = 14.sp,
                            color = TextOnDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Filter chip buttons row — functional with state toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    NoteHubFilterChip(
                        label = filter,
                        isSelected = selectedFilter == filter
                    ) {
                        selectedFilter = filter
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filter uploads based on selected filter
            val filteredUploads = when (selectedFilter) {
                "Images" -> uploads.filter { it.fileType in listOf("PNG", "JPG", "JPEG", "GIF") }
                "Documents" -> uploads.filter { it.fileType in listOf("PDF", "DOC", "TXT", "DOCX") }
                else -> uploads.toList()
            }

            if (filteredUploads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uploads.isEmpty()) "No uploads yet" else "No ${selectedFilter.lowercase()} found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uploads.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap the upload button to add files",
                                fontSize = 14.sp,
                                color = TextTertiary
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUploads, key = { it.id }) { upload ->
                        UploadCard(
                            upload = upload,
                            onDelete = {
                                val name = upload.fileName
                                uploads.remove(upload)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "\"$name\" deleted",
                                        duration = SnackbarDuration.Short
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

@Composable
fun NoteHubFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else CardBackground,
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) TextOnPrimary else TextSecondary
            )
        }
    }
}

@Composable
fun UploadCard(
    upload: Upload,
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = { /* Handle file open */ },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = upload.icon,
                    contentDescription = upload.fileType,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = upload.fileName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = upload.fileSize, fontSize = 13.sp, color = TextSecondary)
                    Text(text = " • ", fontSize = 13.sp, color = TextTertiary)
                    Text(text = upload.uploadDate, fontSize = 13.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = IconPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = ErrorRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Delete", color = ErrorRed)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UploadsScreenPreview() {
    NoteHubTheme {
        UploadsScreen()
    }
}
