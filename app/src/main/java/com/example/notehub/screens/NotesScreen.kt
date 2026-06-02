package com.example.notehub.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import com.example.notehub.data.Note
import com.example.notehub.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.example.notehub.domain.model.LocationNote
import kotlinx.coroutines.launch

/**
 * NotesScreen — Displays a scrollable list of notes.
 * Demonstrates: Scrollable lists (LazyColumn), Card components, and Swipe-to-delete concepts.
 *
 * @param onAddNoteClick  Called when FAB is clicked
 * @param onNoteClick     Called when a note card is clicked (Master-Detail)
 */
@Composable
fun NotesScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (Int) -> Unit = {},
    viewModel: LocationNotesViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Trigger fetching notes from the PHP backend on launch
    LaunchedEffect(Unit) {
        viewModel.fetchNotes()
    }

    val notesList = viewModel.notes.map { note ->
        Note(
            id = note.id,
            title = note.title,
            content = note.description,
            date = note.date,
            category = note.category,
            color = parseHexColor(note.colorHex)
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "My Notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "${notesList.size} items stored securely",
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (notesList.isEmpty()) {
                EmptyNotesView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // extra space for FAB
                ) {
                    items(notesList, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = {
                                scope.launch {
                                    viewModel.deleteLocationNote(note.id) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Note deleted")
                                        }
                                    }
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
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Colored Accent Bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(note.color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Category Tag
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = note.color.copy(alpha = 0.1f),
                ) {
                    Text(
                        text = note.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = note.color,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = note.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = note.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = note.date,
                    fontSize = 12.sp,
                    color = TextTertiary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Options Button
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = TextTertiary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete Note", color = ErrorRed) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = ErrorRed) },
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

@Composable
fun EmptyNotesView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NoteAdd,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No notes found",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Tap the blue button to create one!",
                fontSize = 15.sp,
                color = TextTertiary
            )
        }
    }
}
