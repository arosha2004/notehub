# 📄 DashboardScreen.kt — Explanation

> This is the **Home page** — the first main screen after logging in.

---

## 🧠 What Does This Screen Show?

```
┌────────────────────────────────────────┐
│  ┌──────────────────────────────────┐  │
│  │  Welcome to NoteHub! 🎉          │  │  ← WelcomeCard (gradient bg)
│  │  Your notes are organized...     │  │
│  │  [+ Create New Note]             │  │
│  └──────────────────────────────────┘  │
│                                        │
│  Overview                              │  ← StatsGrid
│  ┌──────────────┐  ┌──────────────┐   │
│  │ 📝 Total Notes│  │ 🕒 This Month│   │
│  │      12      │  │      5       │   │
│  └──────────────┘  └──────────────┘   │
│  ┌──────────────┐  ┌──────────────┐   │
│  │ 📁 Categories│  │ ⬆️ Uploads   │   │
│  │      8       │  │      24      │   │
│  └──────────────┘  └──────────────┘   │
│                                        │
│  Quick Actions                         │  ← QuickActionsSection
│  ┌──────────────┐  ┌──────────────┐   │
│  │  + New Note  │  │ ⬆ Upload File│   │
│  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────┘
```

---

## 🔍 Section-by-Section Explanation

---

### Main DashboardScreen Function

```kotlin
@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        WelcomeCard()
        Spacer(modifier = Modifier.height(24.dp))
        StatsGrid()
        Spacer(modifier = Modifier.height(24.dp))
        QuickActionsSection()
    }
}
```

- The screen is a vertical `Column` that takes the full screen
- `.verticalScroll(rememberScrollState())` — Makes the page scrollable, so on small phones, content doesn't get cut off
- It simply calls 3 sub-components: `WelcomeCard()`, `StatsGrid()`, `QuickActionsSection()`

> 🧠 **This is called component composition** — breaking one screen into smaller, focused pieces. Each function does one job.

---

### WelcomeCard() — The Banner at the Top

```kotlin
@Composable
fun WelcomeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = listOf(GradientStart, GradientEnd)),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(28.dp)
        ) {
            Column {
                Text("Welcome to NoteHub! 🎉", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextOnPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Your notes are organized and ready to go.", color = TextOnDark)
                Spacer(modifier = Modifier.height(20.dp))
                Button(onClick = { /* Navigate to create note */ }) {
                    Icon(Icons.Filled.Add, ...)
                    Text("Create New Note")
                }
            }
        }
    }
}
```

| Code | What it does |
|---|---|
| `Card(containerColor = Color.Transparent)` | The Card itself is invisible — the Box inside handles the background |
| `Brush.linearGradient(GradientStart, GradientEnd)` | Diagonal indigo-to-purple gradient fills the banner |
| `Text(..., color = TextOnPrimary)` | White text that's readable on the dark gradient background |
| `Button(onClick = { })` | The "Create New Note" button — currently does nothing (empty `{}`) |
| `Icon(Icons.Filled.Add)` | The + icon before the button text |

---

### StatsGrid() — The Statistics Section

```kotlin
@Composable
fun StatsGrid() {
    Column {
        Text("Overview", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Total Notes",  value = "12", icon = Icons.Filled.Note,     color = PrimaryBlue,    modifier = Modifier.weight(1f))
            StatCard(title = "This Month",   value = "5",  icon = Icons.Filled.Schedule, color = SuccessGreen,   modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(title = "Categories",   value = "8",  icon = Icons.Filled.Folder,   color = WarningYellow,  modifier = Modifier.weight(1f))
            StatCard(title = "Uploads",      value = "24", icon = Icons.Filled.Upload,   color = InfoBlue,       modifier = Modifier.weight(1f))
        }
    }
}
```

- Creates a 2×2 **grid** of stat cards
- Two `Row`s, each containing 2 `StatCard`s
- `Modifier.weight(1f)` — Each card in a row takes **equal width** (50% each)
- `Arrangement.spacedBy(12.dp)` — 12dp gap between cards
- The values (12, 5, 8, 24) are currently **hardcoded** — not from a real database

---

### StatCard() — Individual Stat Box

```kotlin
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
```

| Code | What it does |
|---|---|
| `modifier: Modifier = Modifier` | Accepts an external modifier (so `weight(1f)` from StatsGrid applies) |
| `color.copy(alpha = 0.12f)` | Creates a very light, transparent version of the color for the icon background |
| `Box(size = 48.dp)` | A rounded square holding the icon |
| `Icon(imageVector = icon, tint = color)` | Shows the icon colored with the card's theme color |
| `Text(text = title)` | Shows the stat label below the icon |

> ⚠️ **Notice:** The `value` parameter (e.g., "12") is passed into `StatCard` but is NOT displayed in the current code. This is a minor bug — the number doesn't show in the card!

---

### QuickActionsSection() — Action Buttons

```kotlin
@Composable
fun QuickActionsSection() {
    Column {
        Text("Quick Actions", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(title = "New Note",    icon = Icons.Filled.Add,    color = PrimaryBlue,  modifier = Modifier.weight(1f))
            QuickActionCard(title = "Upload File", icon = Icons.Filled.Upload, color = SuccessGreen, modifier = Modifier.weight(1f))
        }
    }
}
```

Two colorful action buttons side by side. Both use `weight(1f)` so they share equal space.

---

### QuickActionCard() — Colored Action Button

```kotlin
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
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, tint = TextOnPrimary)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontWeight = FontWeight.Bold, color = TextOnPrimary)
        }
    }
}
```

| Code | What it does |
|---|---|
| `modifier.clickable(onClick = onClick)` | The whole card is tappable |
| `onClick: () -> Unit = {}` | Default is an empty action (does nothing) — intentional for now |
| `containerColor = color` | The entire card background is the passed-in color |
| `tint = TextOnPrimary` | Icon is white (readable on colored background) |
| `Arrangement.Center` | Icon and text are centered in the card |

---

## ⭐ Key Takeaway

> `DashboardScreen.kt` is made of **4 composable functions**:
> 1. `DashboardScreen()` — The main container that calls the others
> 2. `WelcomeCard()` — The gradient banner with a create note button
> 3. `StatsGrid()` + `StatCard()` — A 2×2 grid of stat numbers
> 4. `QuickActionsSection()` + `QuickActionCard()` — Two colored action buttons

All button clicks are currently empty (`{}`) — the functionality will be wired up when a database is added.
