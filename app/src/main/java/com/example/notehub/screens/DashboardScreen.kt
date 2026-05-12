package com.example.notehub.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

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
    onNavigateToUploads: () -> Unit = {}
) {
    // BoxWithConstraints allows us to check the screen width/height at runtime
    // Meeting the "Responsive layouts" requirement
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
                        StatsGrid()
                    }
                }
            } else {
                // PORTRAIT LAYOUT: Stacked vertically
                WelcomeCard(onCreateNoteClick = onNavigateToAddNote)
                Spacer(modifier = Modifier.height(24.dp))
                StatsGrid()
            }

            Spacer(modifier = Modifier.height(24.dp))

            QuickActionsSection(
                onNewNoteClick = onNavigateToAddNote,
                onUploadFileClick = onNavigateToUploads
            )
        }
    }
}

// WelcomeCard — Gradient banner at the top of the Dashboard
// Shows: welcome text + description + "Create New Note" button

@Composable
fun WelcomeCard(
    onCreateNoteClick: () -> Unit = {}
) {
    // Transparent Card shell (the gradient is applied to the Box inside)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // no default card color — gradient is used instead
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // no shadow needed — gradient provides visual depth
        )
    ) {
        // Box with gradient background — the actual visual card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(GradientStart, GradientEnd) // indigo → purple gradient
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(28.dp) // inner padding inside the gradient box
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Welcome heading text
                Text(
                    text = "Welcome to NoteHub! ",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextOnPrimary,         // white text on the dark gradient
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description line
                Text(
                    text = "Your notes are organized and ready to go.",
                    fontSize = 15.sp,
                    color = TextOnDark,            // slightly lighter white/grey
                    lineHeight = 22.sp,
                    letterSpacing = 0.2.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // "Create New Note" button — white button with blue text
                // Navigates to the AddNote screen
                Button(
                    onClick = onCreateNoteClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BackgroundWhite, // white button
                        contentColor = PrimaryBlue        // blue icon and text
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 1.dp
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    // + icon inside the button
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Button label text
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


// StatsGrid — 2x2 grid section showing app statistics
// Row 1: Total Notes | This Month
// Row 2: Categories  | Uploads

@Composable
fun StatsGrid() {
    Column {
        // Section title
        Text(
            text = "Overview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // First row: Total Notes and This Month
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // gap between cards
        ) {
            // weight(1f) makes both cards share the row width equally
            StatCard(
                title = "Total Notes",
                value = "12",
                icon = Icons.Filled.Note,
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "This Month",
                value = "5",
                icon = Icons.Filled.Schedule,
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Second row: Categories and Uploads
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Categories",
                value = "8",
                icon = Icons.Filled.Folder,
                color = WarningYellow,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "Uploads",
                value = "0",
                icon = Icons.Filled.Upload,
                color = InfoBlue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
fun StatCard(
    title: String,                                               // The label shown below the value (e.g. "Total Notes")
    value: String,                                               // The big number shown on the card (e.g. "12")
    icon: androidx.compose.ui.graphics.vector.ImageVector,       // The icon displayed inside the coloured circle box
    color: androidx.compose.ui.graphics.Color,                   // The accent colour: used for the icon box background + icon tint
    modifier: Modifier = Modifier                                // Allows parent to pass layout rules (e.g. equal width using weight)
) {
    Card(
        modifier = modifier, // inherits Modifier.weight(1f) from parent
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // white/card background
        ),
        elevation = CardDefaults.cardElevation( // elevation adds a shadow below the card to make it look raised/lifted
            defaultElevation = 2.dp             // 2.dp = a small/subtle shadow (higher value = deeper shadow)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Colored rounded box containing the icon
            // color.copy(alpha = 0.12f) = very transparent version of the color
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = color.copy(alpha = 0.12f), // soft tinted background
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,               // icon color matches the card's accent color
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The big number (value)
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Stat title ("Total Notes")
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


// QuickActionsSection — Row of large colored action buttons
// Contains: "New Note" (blue) and "Upload File" (green)

@Composable
fun QuickActionsSection(
    onNewNoteClick: () -> Unit = {},
    onUploadFileClick: () -> Unit = {}
) {
    Column {
        // Section title
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Horizontal row of action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp) // gap between buttons
        ) {
            QuickActionCard(
                title = "New Note",
                icon = Icons.Filled.Add,
                color = PrimaryBlue,          // indigo/blue button
                modifier = Modifier.weight(1f), // takes half the row width
                onClick = onNewNoteClick
            )

            QuickActionCard(
                title = "Upload File",
                icon = Icons.Filled.Upload,
                color = SuccessGreen,          // green button
                modifier = Modifier.weight(1f),
                onClick = onUploadFileClick
            )
        }
    }
}


@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {} // default: no-op click handler
) {
    Card(
        modifier = modifier.clickable(onClick = onClick), // card is clickable but does nothing by default
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color // entire card is filled with the given color
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
            horizontalArrangement = Arrangement.Center // icon and text centered horizontally
        ) {
            // Button icon (white)
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = TextOnPrimary,          // white icon
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Button label text (white)
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextOnPrimary,         // white text
                letterSpacing = 0.3.sp
            )
        }
    }
}

//  PREVIEW
// Used only by Android Studio's preview panel — does NOT run on device
@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    NoteHubTheme {
        DashboardScreen()
    }
}
