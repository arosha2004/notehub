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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.platform.LocalContext
import com.example.notehub.ui.security.BiometricHelper
import com.example.notehub.ui.theme.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notehub.ui.viewmodel.LocationNotesViewModel
import com.example.notehub.domain.model.LocationNote
import kotlinx.coroutines.launch

// Displays a scrollable list of notes with live search
@Composable
fun NotesScreen(
    onAddNoteClick: () -> Unit,
    onNoteClick: (Int) -> Unit = {},
    viewModel: LocationNotesViewModel = viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Search state
    var searchQuery by remember { mutableStateOf("") }

    // Security auth states
    var activeAuthNoteId by remember { mutableStateOf<Int?>(null) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    // Trigger fetching notes from the PHP backend on launch
    LaunchedEffect(Unit) {
        viewModel.fetchNotes()
    }

    val notesList = viewModel.notes.map { note ->
        val title = if (note.isSecured) "🔒 Secure Note" else note.title
        val content = if (note.isSecured) "Content is locked. Tap to authenticate." else note.description
        Note(
            id = note.id,
            title = title,
            content = content,
            date = note.date,
            category = note.category,
            color = parseHexColor(note.colorHex)
        )
    }

    // Filter notes based on search query (title, content, or category)
    val filteredNotes = remember(notesList, searchQuery) {
        if (searchQuery.isBlank()) notesList
        else notesList.filter { note ->
            note.title.contains(searchQuery, ignoreCase = true) ||
            note.content.contains(searchQuery, ignoreCase = true) ||
            note.category.contains(searchQuery, ignoreCase = true)
        }
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

            // Live result count
            Text(
                text = if (searchQuery.isBlank())
                    "${notesList.size} items stored securely"
                else
                    "${filteredNotes.size} of ${notesList.size} notes found",
                fontSize = 14.sp,
                color = if (searchQuery.isBlank()) TextSecondary else PrimaryBlue,
                fontWeight = if (searchQuery.isBlank()) FontWeight.Normal else FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // SEARCH BAR
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "Search by title, content or category...",
                        color = TextTertiary,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotBlank()) PrimaryBlue else TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = searchQuery.isNotBlank(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Clear search",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedLabelColor = PrimaryBlue,
                    cursorColor = PrimaryBlue
                )
            )

            // Active filter chip — visible when search is active
            AnimatedVisibility(
                visible = searchQuery.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { searchQuery = "" },
                        label = {
                            Text(
                                text = "\"$searchQuery\"  ✕",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryBlue.copy(alpha = 0.12f),
                            selectedLabelColor = PrimaryBlue
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PrimaryBlue)
                }
            } else if (notesList.isEmpty()) {
                EmptyNotesView()
            } else if (filteredNotes.isEmpty()) {
                // No search results
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = TextTertiary,
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results for \"$searchQuery\"",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching by a different title,\ncontent, or category.",
                            fontSize = 14.sp,
                            color = TextTertiary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        OutlinedButton(
                            onClick = { searchQuery = "" },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue)
                        ) {
                            Text("Clear Search", color = PrimaryBlue, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // extra space for FAB
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                val originalNote = viewModel.notes.find { it.id == note.id }
                                if (originalNote?.isSecured == true) {
                                    activeAuthNoteId = note.id
                                    passwordInput = ""
                                    passwordError = false
                                    val activity = context as? androidx.fragment.app.FragmentActivity
                                    if (activity != null && BiometricHelper.isBiometricAvailable(activity)) {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = activity,
                                            onSuccess = {
                                                onNoteClick(note.id)
                                            },
                                            onError = { error ->
                                                showPasswordDialog = true
                                            }
                                        )
                                    } else {
                                        showPasswordDialog = true
                                    }
                                } else {
                                    onNoteClick(note.id)
                                }
                            },
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
                    color = note.color.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = note.color.copy(alpha = 0.6f)
                    )
                ) {
                    Text(
                        text = note.category.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
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


@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@androidx.compose.runtime.Composable
fun NotesScreenPreview() {
    com.example.notehub.ui.theme.NoteHubTheme {
        NotesScreen(onAddNoteClick = {})
    }
}
