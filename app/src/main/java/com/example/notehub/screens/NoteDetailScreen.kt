package com.example.notehub.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.data.Note
import com.example.notehub.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import kotlinx.coroutines.launch

/**
 * NoteDetailScreen — Displays the full content of a selected note.
 * Part of the Master-Detail interaction requirement.
 *
 * @param noteId  The ID of the note to display
 * @param onNavigateBack  Callback to return to the previous screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    onNavigateBack: () -> Unit,
    onEditClick: (Int) -> Unit = {},
    viewModel: LocationNotesViewModel = viewModel()
) {
    // Find the note in our dynamic ViewModel notes list
    val note = viewModel.notes.find { it.id == noteId }?.let { locNote ->
        Note(
            id = locNote.id,
            title = locNote.title,
            content = locNote.description,
            date = locNote.date,
            category = locNote.category,
            color = parseHexColor(locNote.colorHex)
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Note Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onEditClick(noteId)
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Note")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (note != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Category Chip
                Surface(
                    color = note.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = note.color,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = note.category,
                            color = note.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = note.title,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 34.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Last updated: ${note.date}",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Separator
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Content
                Text(
                    text = note.content,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 28.sp,
                    letterSpacing = 0.2.sp
                )
            }
        } else {
            // Error State
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Note not found")
            }
        }
    }
}
