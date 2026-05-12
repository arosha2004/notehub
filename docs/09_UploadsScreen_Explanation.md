# 📄 UploadsScreen.kt — Explanation

> This is the **Uploads page** — shows files the user has uploaded. Currently shows an empty state.

---

## 🧠 What Does This Screen Show?

**When empty (current state):**
```
┌──────────────────────────────────────┐
│  ┌────────────────────────────────┐  │
│  │  Uploads           0 files    │  │  ← Blue header card
│  └────────────────────────────────┘  │
│                                      │
│           No uploads yet             │  ← Empty state message
│                                ⬆️    │  ← Upload FAB button
└──────────────────────────────────────┘
```

**When uploads exist (not active yet):**
```
│  [Storage Usage Card with progress bar]
│  [All Files] [Images] [Documents]  ← Filter chips
│  ┌──────────────────────────────┐
│  │ 📄 filename.pdf  1.2MB  Date │  ← UploadCard
│  └──────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### The Upload Data Class

```kotlin
data class Upload(
    val id: Int,
    val fileName: String,
    val fileType: String,
    val fileSize: String,
    val uploadDate: String,
    val icon: ImageVector
)
```

A blueprint for what one upload entry contains:

| Property | Example |
|---|---|
| `id` | `1` |
| `fileName` | `"lecture_notes.pdf"` |
| `fileType` | `"PDF"` |
| `fileSize` | `"2.4 MB"` |
| `uploadDate` | `"Feb 16, 2026"` |
| `icon` | `Icons.Filled.PictureAsPdf` |

---

### UploadsScreen Function

```kotlin
@Composable
fun UploadsScreen() {
    val uploads = remember { emptyList<Upload>() }
```

- `emptyList<Upload>()` — The uploads list starts **completely empty**
- This triggers the "No uploads yet" empty state
- In the future, this would be loaded from a database or real file storage

---

### Floating Action Button

```kotlin
FloatingActionButton(
    onClick = { /* Handle upload */ },
    containerColor = PrimaryBlue,
    contentColor = Color.White
) {
    Icon(imageVector = Icons.Filled.Upload, contentDescription = "Upload File")
}
```

The ⬆️ upload FAB button in the bottom right. Currently does nothing (`{ }` empty click handler).

---

### Blue Header Card

```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), ...) {
        Column {
            Text(text = "Uploads", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextOnPrimary)
            Text(text = "${uploads.size} files", fontSize = 14.sp, color = TextOnDark)
        }
    }
}
```

- A solid **blue card** at the top
- Shows "Uploads" title and `${uploads.size} files` (currently "0 files")
- `TextOnPrimary` — White text on the blue background
- `TextOnDark` — Light grey text for the file count

---

### Empty State vs File List

```kotlin
if (uploads.isEmpty()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "No uploads yet", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
} else {
    StorageUsageCard()
    Spacer(...)
    // Filter chips row
    Row { FilterChip("All Files", isSelected = true); FilterChip("Images", ...); FilterChip("Documents", ...) }
    Spacer(...)
    LazyColumn { items(uploads) { upload -> UploadCard(upload = upload) } }
}
```

- **Empty:** Shows centered grey "No uploads yet" text
- **With uploads:** Shows storage card, filter chips, and the list of upload cards

---

### StorageUsageCard() — Storage Display

```kotlin
@Composable
fun StorageUsageCard() {
    Card(...) {
        Column(...) {
            Row(...) {
                Text(text = "Storage Usage", fontWeight = FontWeight.Bold)
                Text(text = "70.3 MB / 5 GB")
            }
            Spacer(...)
            LinearProgressIndicator(
                progress = { 0.014f },  // 70.3 MB / 5000 MB = 1.4%
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = PrimaryBlue,
                trackColor = BorderLight
            )
            Spacer(...)
            Text(text = "1.4% used")
        }
    }
}
```

| Code | What it does |
|---|---|
| `LinearProgressIndicator(progress = { 0.014f })` | A horizontal **progress bar** that fills 1.4% of its width (blue part) |
| `0.014f` — means 1.4% | 70.3 MB out of 5000 MB = 0.014 = 1.4% |
| `clip(RoundedCornerShape(4.dp))` | Makes the progress bar have rounded ends |
| `trackColor = BorderLight` | The background (unfilled) part of the bar is light grey |

> ⚠️ These values (70.3 MB, 1.4%) are **hardcoded**. In a real app, they'd be calculated from actual file data.

---

### FilterChip() — Custom Filter Buttons

```kotlin
@Composable
fun FilterChip(label: String, isSelected: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryBlue else CardBackground,
        modifier = Modifier.height(36.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) TextOnPrimary else TextSecondary
            )
        }
    }
}
```

| Code | What it does |
|---|---|
| `if (isSelected) PrimaryBlue else CardBackground` | Selected chip = blue, unselected chip = white/card background |
| `if (isSelected) TextOnPrimary else TextSecondary` | Selected text = white, unselected text = grey |
| `RoundedCornerShape(20.dp)` | Very rounded, pill-shaped chip |

> ⚠️ Note: These chips currently have **no click handler** — they're visual only. The "All Files" chip is always shown as selected.

---

### UploadCard() — Individual File Entry

```kotlin
@Composable
fun UploadCard(upload: Upload) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            // File icon box
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f))
            ) {
                Icon(imageVector = upload.icon, tint = PrimaryBlue)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(text = upload.fileName, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row {
                    Text(text = upload.fileSize)
                    Text(text = " • ")
                    Text(text = upload.uploadDate)
                }
            }

            // More options
            IconButton(onClick = { /* Show options */ }) {
                Icon(imageVector = Icons.Filled.MoreVert)
            }
        }
    }
}
```

The `UploadCard` layout:
```
┌──────────────────────────────────────────────────┐
│  [📄]  filename.pdf (max 1 line...)        [⋮]  │
│        2.4 MB • Feb 16, 2026                     │
└──────────────────────────────────────────────────┘
   ↑          ↑                             ↑
File icon  File name & size/date         3-dot menu
```

| Code | What it does |
|---|---|
| `PrimaryBlue.copy(alpha = 0.1f)` | Very light blue background for the icon box |
| `weight(1f)` on Column | Info column takes all available space between icon and ⋮ button |
| `TextOverflow.Ellipsis` | Long file names get "..." instead of overflowing |
| `" • "` separator | The dot between file size and date |

---

## ⭐ Key Takeaway

> `UploadsScreen.kt` shows:
> 1. A **blue header card** with the file count
> 2. An **empty state** message when no files exist (current state)
> 3. When files exist: storage progress bar, filter chips, and a list of `UploadCard`s
>
> All upload functionality is still **placeholder** — the FAB and filter chips don't do anything yet.
