package com.example.notehub.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*

/**
 * DashboardScreen — the main home screen after login.
 * Scrollable vertical layout containing: WelcomeCard, StatsGrid, QuickActionsSection.
 *
 * @param onNavigateToAddNote  Called when the user taps "Create New Note" or the "New Note" quick action
 * @param onNavigateToUploads  Called when the user taps the "Upload File" quick action
 */
@Composable
fun DashboardScreen(
    onNavigateToAddNote: () -> Unit = {},
    onNavigateToUploads: () -> Unit = {},
    onNavigateToLocationNotes: () -> Unit = {},
    viewModel: LocationNotesViewModel = viewModel()
) {
    // Trigger fetching notes from the PHP backend on launch
    LaunchedEffect(Unit) {
        viewModel.fetchNotes()
    }

    val totalNotes = viewModel.notes.size
    val currentYearMonth = remember {
        java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date())
    }
    val notesThisMonth = viewModel.notes.count { it.date.startsWith(currentYearMonth) }
    val categoriesCount = viewModel.notes.map { it.category }.distinct().size
    val uploadsCount = viewModel.uploads.size

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isLandscape = maxWidth > maxHeight

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (isLandscape) {
                // LANDSCAPE LAYOUT: WelcomeCard and Stats in a Row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        WelcomeCard(onCreateNoteClick = onNavigateToAddNote)
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        StatsGrid(
                            totalNotes = totalNotes,
                            notesThisMonth = notesThisMonth,
                            categoriesCount = categoriesCount,
                            uploadsCount = uploadsCount
                        )
                    }
                }
            } else {
                // PORTRAIT LAYOUT: Stacked vertically
                WelcomeCard(onCreateNoteClick = onNavigateToAddNote)
                Spacer(modifier = Modifier.height(24.dp))
                StatsGrid(
                    totalNotes = totalNotes,
                    notesThisMonth = notesThisMonth,
                    categoriesCount = categoriesCount,
                    uploadsCount = uploadsCount
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            QuickActionsSection(
                onNewNoteClick = onNavigateToAddNote,
                onUploadFileClick = onNavigateToUploads,
                onLocationMapClick = onNavigateToLocationNotes
            )
        }
    }
}

@Composable
fun WelcomeCard(
    onCreateNoteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Welcome to NoteHub! ",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your notes are organized and ready to go.",
                    fontSize = 15.sp,
                    color = TextOnDark,
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onCreateNoteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackgroundWhite,
                        contentColor = PrimaryBlue
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 1.dp
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create New Note",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun StatsGrid(
    totalNotes: Int,
    notesThisMonth: Int,
    categoriesCount: Int,
    uploadsCount: Int
) {
    Column {
        Text(
            text = "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Notes",
                value = totalNotes.toString(),
                icon = Icons.AutoMirrored.Filled.Note,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "This Month",
                value = notesThisMonth.toString(),
                icon = Icons.Filled.Schedule,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Categories",
                value = categoriesCount.toString(),
                icon = Icons.Filled.Folder,
                color = WarningYellow,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Uploads",
                value = uploadsCount.toString(),
                icon = Icons.Filled.Upload,
                color = InfoBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onNewNoteClick: () -> Unit = {},
    onUploadFileClick: () -> Unit = {},
    onLocationMapClick: () -> Unit = {}
) {
    Column {
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "New Note",
                icon = Icons.Filled.Add,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f),
                onClick = onNewNoteClick
            )

            QuickActionCard(
                title = "Location Map",
                icon = Icons.Filled.LocationOn,
                color = AccentPurple,
                modifier = Modifier.weight(1f),
                onClick = onLocationMapClick
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActionCard(
                title = "Upload File",
                icon = Icons.Filled.Upload,
                color = SuccessGreen,
                modifier = Modifier.fillMaxWidth(),
                onClick = onUploadFileClick
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextOnPrimary,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,
                letterSpacing = 0.3.sp
            )
        }
    }
}
