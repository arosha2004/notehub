package com.example.notehub.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notehub.data.Note
import com.example.notehub.data.SampleData
import com.example.notehub.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PrimaryBlue) }
    var selectedCategory by remember { mutableStateOf("Personal") }
    var isPinned by remember { mutableStateOf(false) }

    val categories = listOf("Personal", "Work", "Study", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Note", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank() && content.isNotBlank()) {
                                val newNote = Note(
                                    id = (SampleData.notes.maxOfOrNull { it.id } ?: 0) + 1,
                                    title = title,
                                    content = content,
                                    date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
                                    category = selectedCategory,
                                    color = selectedColor
                                )
                                SampleData.notes.add(0, newNote)
                                onNavigateBack()
                            }
                        },
                        enabled = title.isNotBlank() && content.isNotBlank()
                    ) {
                        Text(
                            "Save",
                            fontWeight = FontWeight.Bold,
                            color = if (title.isNotBlank() && content.isNotBlank()) PrimaryBlue else TextTertiary
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
                .padding(16.dp)
        ) {
            Text(
                text = "Category",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = selectedColor.copy(alpha = 0.1f),
                            selectedLabelColor = selectedColor,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedCategory == category) selectedColor else BorderMedium,
                            selectedBorderColor = selectedColor,
                            enabled = true,
                            selected = selectedCategory == category
                        )
                    )
                }
            }

            // ── COLOR PICKER ROW ───────────────────────────────────────
            Text(
                text = "Note Color",
                fontSize = 14.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val noteColors = listOf(PrimaryBlue, SuccessGreen, WarningYellow, InfoBlue, AccentPurple, ErrorRed)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                noteColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(if (selectedColor == color) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) WarningYellow else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Pin to top",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Switch(
                    checked = isPinned,
                    onCheckedChange = { isPinned = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BackgroundWhite,
                        checkedTrackColor = PrimaryBlue
                    )
                )
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = BorderMedium,
                    focusedLabelColor = selectedColor
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = selectedColor,
                    unfocusedBorderColor = BorderMedium,
                    focusedLabelColor = selectedColor
                ),
                shape = RoundedCornerShape(12.dp),
                minLines = 5
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddNoteScreenPreview() {
    NoteHubTheme {
        AddNoteScreen(onNavigateBack = {})
    }
}
