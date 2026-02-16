package com.example.notehub.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

// Data class for Upload
data class Upload(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val uploadDate: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun UploadsScreen() {
    // Empty uploads list
    val uploads = remember {
        emptyList<Upload>()
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Handle upload */ },
                containerColor = PrimaryBlue,
                contentColor = androidx.compose.ui.graphics.Color.White
            ) {
                Icon(
                    imageVector = Icons.Filled.Upload,
                    contentDescription = "Upload File"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header Card
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
            
            // Empty state
            if (uploads.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No uploads yet",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Storage Usage Card
                StorageUsageCard()
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Filter Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        label = "All Files",
                        isSelected = true
                    )
                    FilterChip(
                        label = "Images",
                        isSelected = false
                    )
                    FilterChip(
                        label = "Documents",
                        isSelected = false
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Uploads List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uploads) { upload ->
                        UploadCard(upload = upload)
                    }
                }
            }
        }
    }
}

@Composable
fun StorageUsageCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage Usage",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "70.3 MB / 5 GB",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { 0.014f }, // 70.3 MB / 5000 MB
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = PrimaryBlue,
                trackColor = BorderLight,
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "1.4% used",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean
) {
    Surface(
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
fun UploadCard(upload: Upload) {
    Card(
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
            // File Icon
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
            
            // File Info
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
                    Text(
                        text = upload.fileSize,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = " • ",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = upload.uploadDate,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // More Options Button
            IconButton(
                onClick = { /* Show options */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More options",
                    tint = IconPrimary,
                    modifier = Modifier.size(20.dp)
                )
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
