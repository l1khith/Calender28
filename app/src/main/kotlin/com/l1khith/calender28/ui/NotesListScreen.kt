package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.viewmodel.AppViewModelProvider
import com.l1khith.calender28.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Tag colors specified for linked entities
private val HabitTagColor = Color(0xFFF97316)  // Orange
private val TaskTagColor = Color(0xFF10B981)   // Green
private val CycleTagColor = Color(0xFF3B82F6)  // Blue
private val UnsortedTagColor = Color(0xFF94A3B8) // Gray

private val NoteAccentColors = listOf(
    null,
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Green
    Color(0xFFF97316), // Orange
    Color(0xFF8B5CF6), // Purple
    Color(0xFFEF4444)  // Crimson
)

private val FilterCategories = listOf(
    "ALL" to "All Notes",
    "HABIT" to "Habits",
    "TASK" to "Tasks",
    "CYCLE" to "Cycles",
    "NOTE" to "General"
)

@Composable
fun NotesListScreen(
    notesViewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory),
    isProActive: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    createNoteTrigger: Int = 0
) {
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by notesViewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by notesViewModel.selectedFilter.collectAsStateWithLifecycle()

    var activeNoteForEdit by remember { mutableStateOf<Note?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(createNoteTrigger) {
        if (createNoteTrigger > 0) {
            showCreateDialog = true
        }
    }

    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val unpinnedNotes = remember(notes) { notes.filter { !it.isPinned } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { notesViewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MatrixShapes.Pill),
                placeholder = {
                    Text(
                        text = "Search by title or content...",
                        color = MatrixColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MatrixColors.Primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { notesViewModel.clearSearch() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MatrixColors.TextSecondary
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MatrixShapes.Pill,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MatrixColors.SurfaceContainerLow,
                    unfocusedContainerColor = MatrixColors.SurfaceContainerLow,
                    focusedBorderColor = MatrixColors.Primary,
                    unfocusedBorderColor = MatrixColors.OutlineVariant,
                    focusedTextColor = MatrixColors.TextHeader,
                    unfocusedTextColor = MatrixColors.TextHeader
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(FilterCategories) { (filterKey, filterLabel) ->
                    val isSelected = selectedFilter == filterKey
                    Surface(
                        shape = MatrixShapes.Pill,
                        color = if (isSelected) MatrixColors.PrimaryContainer else MatrixColors.SurfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.clickable { notesViewModel.setFilter(filterKey) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            val filterIcon: ImageVector? = when (filterKey) {
                                "HABIT" -> AppIcons.Fire
                                "TASK" -> Icons.Default.CheckCircle
                                "CYCLE" -> Icons.Default.Sync
                                "NOTE" -> Icons.Outlined.Description
                                else -> null
                            }

                            if (filterIcon != null) {
                                Icon(
                                    imageVector = filterIcon,
                                    contentDescription = null,
                                    tint = if (isSelected) MatrixColors.OnPrimaryContainer else MatrixColors.TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Text(
                                text = filterLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (isSelected) MatrixColors.OnPrimaryContainer else MatrixColors.TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes Count Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Notes",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ALL NOTES",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MatrixColors.TextHeader
                    )
                }

                Surface(
                    shape = MatrixShapes.Pill,
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${notes.size} notes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MatrixColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notes List or Empty State
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MatrixColors.SurfaceContainerHigh,
                            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = "No Notes",
                                    tint = MatrixColors.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) "No Matching Notes" else "No Notes Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MatrixColors.TextHeader
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (searchQuery.isNotBlank()) {
                                "No notes found matching your search term."
                            } else {
                                "Tap + to create your first note."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MatrixColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreateDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MatrixColors.Primary,
                                    contentColor = MatrixColors.OnPrimary
                                ),
                                shape = MatrixShapes.Pill
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Note",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Note")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    // Pinned notes section
                    if (pinnedNotes.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = MatrixColors.Primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PINNED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MatrixColors.Primary
                                )
                            }
                        }

                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                onClick = { activeNoteForEdit = note },
                                onTogglePin = { notesViewModel.togglePin(note) },
                                onDelete = { notesViewModel.deleteNote(note) }
                            )
                        }

                        if (unpinnedNotes.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "OTHER NOTES",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MatrixColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Unpinned notes
                    items(unpinnedNotes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { activeNoteForEdit = note },
                            onTogglePin = { notesViewModel.togglePin(note) },
                            onDelete = { notesViewModel.deleteNote(note) }
                        )
                    }
                }
            }
        }

        // Note Editor Dialog
        if (showCreateDialog || activeNoteForEdit != null) {
            val noteToEdit = activeNoteForEdit
            NoteEditorDialog(
                note = noteToEdit,
                onDismiss = {
                    showCreateDialog = false
                    activeNoteForEdit = null
                },
                onSave = { title, content, colorHex, linkedEntity, isMarkdown ->
                    notesViewModel.saveNote(
                        title = title,
                        content = content,
                        existingNote = noteToEdit,
                        colorHex = colorHex,
                        linkedEntity = linkedEntity,
                        isMarkdown = isMarkdown
                    )
                    showCreateDialog = false
                    activeNoteForEdit = null
                },
                onDelete = {
                    if (noteToEdit != null) {
                        notesViewModel.deleteNote(noteToEdit)
                    }
                    showCreateDialog = false
                    activeNoteForEdit = null
                }
            )
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onTogglePin: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val tagColor = when (note.linkedEntity.uppercase()) {
        "HABIT" -> HabitTagColor
        "TASK" -> TaskTagColor
        "CYCLE" -> CycleTagColor
        else -> UnsortedTagColor
    }

    val tagIcon: ImageVector = when (note.linkedEntity.uppercase()) {
        "HABIT" -> AppIcons.Fire
        "TASK" -> Icons.Default.CheckCircle
        "CYCLE" -> Icons.Default.Sync
        else -> Icons.Outlined.Description
    }

    val tagLabel = when (note.linkedEntity.uppercase()) {
        "HABIT" -> "Habit"
        "TASK" -> "Task"
        "CYCLE" -> "Cycle"
        else -> "Note"
    }

    val formattedDate = remember(note.updatedAtMs) {
        val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        sdf.format(Date(note.updatedAtMs))
    }

    Surface(
        shape = MatrixShapes.CardMedium,
        color = MatrixColors.SurfaceContainerLow,
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Title + Format Badge + Pin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = tagIcon,
                        contentDescription = tagLabel,
                        tint = tagColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (note.title.isNotBlank()) note.title else "Untitled Note",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MatrixColors.TextHeader,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Format Badge (MD or TXT)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (note.isMarkdown) MatrixColors.PrimaryContainer else MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        if (note.isMarkdown) MatrixColors.Primary else MatrixColors.OutlineVariant
                    )
                ) {
                    Text(
                        text = if (note.isMarkdown) "MD" else "TXT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = if (note.isMarkdown) MatrixColors.Primary else MatrixColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (note.isPinned) "Unpin note" else "Pin note",
                        tint = if (note.isPinned) MatrixColors.Primary else MatrixColors.TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            // Content Preview (first 2-3 lines)
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MatrixColors.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Linked Entity Tag + Date + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Entity Tag Chip
                Surface(
                    shape = MatrixShapes.Pill,
                    color = tagColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.35f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = tagIcon,
                            contentDescription = null,
                            tint = tagColor,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tagLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            ),
                            color = tagColor
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MatrixColors.TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = MatrixColors.TextSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteEditorDialog(
    note: Note?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, colorHex: String?, linkedEntity: String, isMarkdown: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    var linkedEntity by remember { mutableStateOf(note?.linkedEntity ?: "NOTE") }
    var isMarkdown by remember { mutableStateOf(note?.isMarkdown ?: false) }
    var selectedColor by remember {
        mutableStateOf(
            note?.colorHex?.let {
                try {
                    Color(android.graphics.Color.parseColor(it))
                } catch (_: Exception) {
                    null
                }
            }
        )
    }

    val entities = listOf(
        "NOTE" to "General",
        "HABIT" to "Habit",
        "TASK" to "Task",
        "CYCLE" to "Cycle"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MatrixShapes.CardLarge,
            color = MatrixColors.SurfaceContainerLow,
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (note == null) "New Note" else "Edit Note",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MatrixColors.TextHeader
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Format toggle button (MD / TXT)
                        FilterChip(
                            selected = isMarkdown,
                            onClick = { isMarkdown = !isMarkdown },
                            label = {
                                Text(
                                    text = if (isMarkdown) "Markdown" else "Plain Text",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = MatrixShapes.Pill
                        )

                        if (note != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Note title", color = MatrixColors.TextSecondary)
                    },
                    singleLine = true,
                    shape = MatrixShapes.CardSmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MatrixColors.Surface,
                        unfocusedContainerColor = MatrixColors.Surface,
                        focusedBorderColor = MatrixColors.Primary,
                        unfocusedBorderColor = MatrixColors.OutlineVariant,
                        focusedTextColor = MatrixColors.TextHeader,
                        unfocusedTextColor = MatrixColors.TextHeader
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Content Input
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp),
                    placeholder = {
                        Text("Start typing your note...", color = MatrixColors.TextSecondary)
                    },
                    shape = MatrixShapes.CardSmall,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MatrixColors.Surface,
                        unfocusedContainerColor = MatrixColors.Surface,
                        focusedBorderColor = MatrixColors.Primary,
                        unfocusedBorderColor = MatrixColors.OutlineVariant,
                        focusedTextColor = MatrixColors.TextHeader,
                        unfocusedTextColor = MatrixColors.TextHeader
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Linked Entity Selector
                Text(
                    text = "Link to Entity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MatrixColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    entities.forEach { (entityKey, entityTitle) ->
                        val isSelected = linkedEntity == entityKey
                        val chipColor = when (entityKey) {
                            "HABIT" -> HabitTagColor
                            "TASK" -> TaskTagColor
                            "CYCLE" -> CycleTagColor
                            else -> UnsortedTagColor
                        }

                        Surface(
                            shape = MatrixShapes.Pill,
                            color = if (isSelected) chipColor.copy(alpha = 0.2f) else MatrixColors.Surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) chipColor else MatrixColors.OutlineVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable { linkedEntity = entityKey }
                        ) {
                            Text(
                                text = entityTitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) chipColor else MatrixColors.TextSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color accent picker
                Text(
                    text = "Accent Color",
                    style = MaterialTheme.typography.labelSmall,
                    color = MatrixColors.TextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteAccentColors.forEach { color ->
                        val isSelected = selectedColor == color
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(color ?: MatrixColors.SurfaceContainerHigh)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == null && !isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MatrixColors.TextSecondary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        shape = MatrixShapes.Pill
                    ) {
                        Text("Cancel", color = MatrixColors.TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val hex = selectedColor?.let { c ->
                                String.format("#%06X", (0xFFFFFF and c.hashCode()))
                            }
                            onSave(title, content, hex, linkedEntity, isMarkdown)
                        },
                        enabled = title.isNotBlank() || content.isNotBlank(),
                        shape = MatrixShapes.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = MatrixColors.OnPrimary
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
