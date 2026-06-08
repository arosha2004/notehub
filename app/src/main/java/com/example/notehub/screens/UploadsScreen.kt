package com.example.notehub.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.notehub.domain.model.Upload
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper to query file name and size from Uri
 */
private fun queryUriMetadata(context: Context, uri: Uri): Pair<String, Long> {
    var name = "unknown"
    var size = 0L
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    if (name == "unknown") {
        name = uri.lastPathSegment ?: "file"
    }
    return Pair(name, size)
}

/**
 * Format file size into human readable string
 */
private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

/**
 * UploadsScreen — Shows the list of uploaded files.
 */
@Composable
fun UploadsScreen(
    viewModel: LocationNotesViewModel = viewModel()
) {
    val uploads = viewModel.uploads

    // State for filter chip selection
    var selectedFilter by remember { mutableStateOf("All Files") }
    val filterOptions = listOf("All Files", "Images", "Documents")

    // Snackbar host for user feedback
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var previewUpload by remember { mutableStateOf<Upload?>(null) }

    // Set up file picker launcher to pick files from storage
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, takeFlags)
            } catch (e: SecurityException) {
                // Ignore if it fails
            }
            val (name, size) = queryUriMetadata(context, uri)
            val extension = name.substringAfterLast('.', "").uppercase()
            val isImage = extension in listOf("PNG", "JPG", "JPEG", "GIF", "WEBP")
            val isDoc = extension in listOf("PDF", "DOC", "DOCX", "TXT", "XLS", "XLSX", "PPT", "PPTX")
            val fileType = if (extension.isNotEmpty()) extension else "FILE"
            val icon = if (isImage) Icons.Filled.Image else if (isDoc) Icons.Filled.Description else Icons.Filled.AttachFile
            
            val formattedDate = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date())
            val formattedSize = formatFileSize(size)
            
            val newId = (uploads.maxOfOrNull { it.id } ?: 0) + 1
            val newUpload = Upload(
                id = newId,
                fileName = name,
                fileType = fileType,
                fileSize = formattedSize,
                uploadDate = formattedDate,
                icon = icon,
                uri = uri
            )
            uploads.add(newUpload)
            viewModel.saveUploads()
            
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "File \"$name\" uploaded successfully!",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    if (previewUpload != null) {
        val upload = previewUpload!!
        AlertDialog(
            onDismissRequest = { previewUpload = null },
            title = {
                Text(
                    text = upload.fileName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isImage = upload.fileType in listOf("PNG", "JPG", "JPEG", "GIF", "WEBP")
                    if (isImage && upload.uri != null) {
                        coil.compose.AsyncImage(
                            model = upload.uri,
                            contentDescription = "Image Preview",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PrimaryBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = upload.icon,
                                contentDescription = upload.fileType,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Type: ${upload.fileType} File",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Size: ${upload.fileSize}",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (upload.uri != null) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(upload.uri, context.contentResolver.getType(upload.uri) ?: "*/*")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("No app to open this file")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open File")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { previewUpload = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    // Scaffold provides the screen layout + floating action button placement
    Scaffold(
        // FLOATING ACTION BUTTON
        // Blue upload (⬆) button in the bottom right corner
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("*/*"))
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
                            onClick = { previewUpload = upload },
                            onDelete = {
                                val name = upload.fileName
                                uploads.remove(upload)
                                viewModel.saveUploads()
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
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
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
