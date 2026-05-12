# 📄 NotesScreen.kt — Explanation

> This is the **Notes List page** — shows all your saved notes.

---

## 🧠 What Does This Screen Show?

```
┌─────────────────────────────────────┐
│  All Notes (1)                      │  ← Count of notes
│                                     │
│  ┌───────────────────────────────┐  │
│  │ █ [Study]  MAD           ⋮   │  │  ← NoteCard
│  │   Mobile application dev...  │  │
│  │   Feb 16, 2026                │  │
│  └───────────────────────────────┘  │
│                                     │
│                              [+]    │  ← Floating Action Button
└─────────────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### The Note Data Class

```kotlin
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val date: String,
    val category: String,
    val color: androidx.compose.ui.graphics.Color
)
```

- `data class` — A class designed purely to **hold data**. Like a blueprint for what a Note contains.
- Each `Note` has 6 properties:

| Property | Type | Example |
|---|---|---|
| `id` | Number | `1` |
| `title` | Text | `"MAD"` |
| `content` | Text | `"Mobile application development..."` |
| `date` | Text | `"Feb 16, 2026"` |
| `category` | Text | `"Study"` |
| `color` | Color | `PrimaryBlue` |

> 🧠 **Analogy:** A `data class` is like a form template. Every note fills in the same fields.

---

### NotesScreen Function

```kotlin
@Composable
fun NotesScreen(
    onAddNoteClick: () -> Unit
) {
```

- `onAddNoteClick` — An action passed in from `MainScreen.kt`. When called, it navigates to `AddNoteScreen`.

---

### The Notes List (Currently 1 Note)

```kotlin
val notes = remember {
    listOf(
        Note(
            id = 1,
            title = "MAD",
            content = "Mobile application development is good subject",
            date = "Feb 16, 2026",
            category = "Study",
            color = PrimaryBlue
        )
    )
}
```

- `remember { listOf(...) }` — Creates a fixed list of notes kept in memory
- Currently has **1 hardcoded note** named "MAD" about Mobile Application Development
- In the future, this would come from a **database** (Room) instead

---

### Floating Action Button (FAB)

```kotlin
Scaffold(
    floatingActionButton = {
        FloatingActionButton(
            onClick = onAddNoteClick,
            containerColor = PrimaryBlue,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Note")
        }
    }
) { paddingValues -> ... }
```

- `FloatingActionButton` — The round **+** button that floats over content (bottom-right corner)
- `onClick = onAddNoteClick` — Tapping it takes the user to `AddNoteScreen`
- `containerColor = PrimaryBlue` — Blue button background
- `contentColor = White` — White + icon inside

---

### Box & Column Layout

```kotlin
Box(
    modifier = Modifier.fillMaxSize().padding(paddingValues).background(...)
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "All Notes (${notes.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        // ... note list or empty state
    }
}
```

- `"All Notes (${notes.size})"` — The `${notes.size}` part is a **string template**. It dynamically inserts the count of notes. If there's 1 note → shows "All Notes (1)"

---

### Empty State vs Notes List

```kotlin
if (notes.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No notes yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
} else {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(notes) { note ->
            NoteCard(note = note)
        }
    }
}
```

| Code | What it does |
|---|---|
| `if (notes.isEmpty())` | If the list is empty, show the empty state message |
| `Text("No notes yet")` | Centered grey message when there are no notes |
| `else { LazyColumn(...) }` | If there ARE notes, show them in a scrollable list |
| `LazyColumn` | A **smart list** — only renders notes visible on screen (efficient for large lists) |
| `Arrangement.spacedBy(12.dp)` | 12dp gap between each note card |
| `contentPadding = PaddingValues(bottom = 16.dp)` | Extra space at the bottom so last note isn't hidden behind the FAB |
| `items(notes) { note -> NoteCard(note = note) }` | For each note in the list, draw a `NoteCard` |

> 🧠 **LazyColumn vs Column:** A regular `Column` renders ALL items even if they're off-screen. `LazyColumn` only renders what's visible — much better for performance with many notes.

---

### NoteCard() — Individual Note Display

```kotlin
@Composable
fun NoteCard(note: Note) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* Navigate to note detail */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {

            // Color indicator bar (left side)
            Box(
                modifier = Modifier
                    .width(4.dp).height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(note.color)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Category chip
                Surface(shape = RoundedCornerShape(4.dp), color = note.color.copy(alpha = 0.15f)) {
                    Text(text = note.category, color = note.color, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(text = note.title, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                     maxLines = 1, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(4.dp))

                // Content preview
                Text(text = note.content, fontSize = 14.sp,
                     maxLines = 2, overflow = TextOverflow.Ellipsis)

                Spacer(modifier = Modifier.height(8.dp))

                // Date
                Text(text = note.date, fontSize = 12.sp, color = TextTertiary)
            }

            // More options button
            IconButton(onClick = { /* Show options menu */ }, modifier = Modifier.size(32.dp)) {
                Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "More options")
            }
        }
    }
}
```

Breaking down the `NoteCard` visually:

```
┌──────────────────────────────────────────────────┐
│ █  [Study tag]                             [⋮]  │
│ █  Note Title (bold, max 1 line)                 │
│ █  Note content preview (max 2 lines, ...)       │
│    Feb 16, 2026                                  │
└──────────────────────────────────────────────────┘
  ↑           ↑                                 ↑
Color bar   Content Column              More options button
```

| Code | What it does |
|---|---|
| `Box(width = 4.dp, height = 60.dp)` | The thin colored vertical bar on the left |
| `.background(note.color)` | Bar color matches the note's assigned color |
| `Surface(color = note.color.copy(alpha = 0.15f))` | Very light tinted background for the category tag pill |
| `maxLines = 1, overflow = TextOverflow.Ellipsis` | Title cuts off with "..." if too long |
| `maxLines = 2, overflow = TextOverflow.Ellipsis` | Content shows max 2 lines then "..." |
| `Icons.Filled.MoreVert` | The ⋮ three-dot menu icon (currently does nothing) |
| `.weight(1f)` on Column | The content column takes all available space, pushing ⋮ to the right |

---

## ⭐ Key Takeaway

> `NotesScreen.kt` does 3 things:
> 1. Defines what a `Note` data object looks like (data class)
> 2. Shows a scrollable list of `NoteCard`s (or an empty message)
> 3. Has a **+** FAB button to go to `AddNoteScreen`

Currently uses **hardcoded sample data**. The next step would be connecting this to a real database.
