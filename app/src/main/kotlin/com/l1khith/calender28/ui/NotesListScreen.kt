package com.l1khith.calender28.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.ui.notes.NoteCard
import com.l1khith.calender28.ui.notes.NoteEditorScreen
import com.l1khith.calender28.ui.notes.NotePreviewScreen
import com.l1khith.calender28.ui.notes.graph.GraphViewScreen
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.viewmodel.AppViewModelProvider
import com.l1khith.calender28.viewmodel.NotesViewModel

@Composable
fun NotesListScreen(
    notesViewModel: NotesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = AppViewModelProvider.Factory),
    createNoteTrigger: Int = 0,
    modifier: Modifier = Modifier
) {
    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val searchQuery by notesViewModel.searchQuery.collectAsStateWithLifecycle()

    val activeNote by notesViewModel.activeNote.collectAsStateWithLifecycle()
    val draftTitle by notesViewModel.draftTitle.collectAsStateWithLifecycle()
    val draftContent by notesViewModel.draftContent.collectAsStateWithLifecycle()
    val draftFormat by notesViewModel.draftFormat.collectAsStateWithLifecycle()
    val isPreviewMode by notesViewModel.isPreviewMode.collectAsStateWithLifecycle()

    // Graph View Sub-screens
    var showGlobalGraph by remember { mutableStateOf(false) }
    var isSelectionMode by remember { mutableStateOf(false) }
    val selectedNoteIds = remember { mutableStateListOf<String>() }
    var batchGraphTargetIds by remember { mutableStateOf<Set<String>?>(null) }

    // Trigger creating note from external triggers if updated
    LaunchedEffect(createNoteTrigger) {
        if (createNoteTrigger > 0) {
            notesViewModel.createNewNote()
        }
    }

    // Handle Predictive Back / Android Back Press (auto-saves note)
    BackHandler(enabled = activeNote != null || showGlobalGraph || batchGraphTargetIds != null || isSelectionMode) {
        if (activeNote != null) {
            if (isPreviewMode) {
                notesViewModel.setPreviewMode(false)
            } else {
                notesViewModel.saveAndCloseEditor()
            }
        } else if (batchGraphTargetIds != null) {
            batchGraphTargetIds = null
        } else if (showGlobalGraph) {
            showGlobalGraph = false
        } else if (isSelectionMode) {
            isSelectionMode = false
            selectedNoteIds.clear()
        }
    }

    // ─── Sub-Screen: Batch Graph View ──────────────────────────────────
    if (batchGraphTargetIds != null) {
        GraphViewScreen(
            onBack = { batchGraphTargetIds = null },
            onOpenNote = { targetNoteId ->
                batchGraphTargetIds = null
                val targetNote = notes.find { it.id == targetNoteId }
                if (targetNote != null) {
                    notesViewModel.openNoteForEdit(targetNote)
                }
            },
            batchSelectedIds = batchGraphTargetIds,
            modifier = modifier
        )
        return
    }

    // ─── Sub-Screen: Global Notes Graph View ────────────────────────────
    if (showGlobalGraph) {
        GraphViewScreen(
            onBack = { showGlobalGraph = false },
            onOpenNote = { targetNoteId ->
                showGlobalGraph = false
                val targetNote = notes.find { it.id == targetNoteId }
                if (targetNote != null) {
                    notesViewModel.openNoteForEdit(targetNote)
                }
            },
            batchSelectedIds = null,
            modifier = modifier
        )
        return
    }

    // ─── 1. Sub-Screen: Note Preview Screen ──────────────────────────────
    if (activeNote != null && isPreviewMode) {
        val previewDraft = activeNote!!.copy(
            title = draftTitle,
            content = draftContent,
            format = draftFormat
        )
        NotePreviewScreen(
            note = previewDraft,
            onBack = { notesViewModel.setPreviewMode(false) },
            onDelete = {
                notesViewModel.deleteActiveNote()
            },
            modifier = modifier
        )
        return
    }

    // ─── 2. Sub-Screen: Note Editor Screen ───────────────────────────────
    if (activeNote != null) {
        val isExisting = notes.any { it.id == activeNote!!.id }
        val noteForMenu = activeNote!!.copy(
            title = draftTitle,
            content = draftContent,
            format = draftFormat
        )
        NoteEditorScreen(
            title = draftTitle,
            content = draftContent,
            format = draftFormat,
            isExistingNote = isExisting,
            noteForMenu = noteForMenu,
            onTitleChange = { notesViewModel.updateTitle(it) },
            onContentChange = { notesViewModel.updateContent(it) },
            onToggleFormat = { notesViewModel.toggleFormat() },
            onPreviewClick = { notesViewModel.setPreviewMode(true) },
            onBack = { notesViewModel.saveAndCloseEditor() },
            onDelete = {
                notesViewModel.deleteActiveNote()
            },
            modifier = modifier
        )
        return
    }

    // ─── 3. Main Screen: Notes List Screen ───────────────────────────────
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── Search & Mode Actions Bar ────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { notesViewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search notes...",
                            fontSize = 14.sp,
                            color = MatrixColors.TextSecondary.copy(alpha = 0.6f)
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
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MatrixColors.Primary,
                        unfocusedBorderColor = MatrixColors.OutlineVariant,
                        focusedTextColor = MatrixColors.TextHeader,
                        unfocusedTextColor = MatrixColors.TextHeader,
                        focusedContainerColor = MatrixColors.SurfaceContainerLow,
                        unfocusedContainerColor = MatrixColors.SurfaceContainerLow
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Knowledge Graph Button
                IconButton(
                    onClick = { showGlobalGraph = true },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MatrixColors.SurfaceContainerLow, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoGraph,
                        contentDescription = "Knowledge Graph",
                        tint = MatrixColors.Primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Batch Selection Toggle Button
                IconButton(
                    onClick = {
                        isSelectionMode = !isSelectionMode
                        if (!isSelectionMode) selectedNoteIds.clear()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (isSelectionMode) MatrixColors.Primary.copy(alpha = 0.2f) else MatrixColors.SurfaceContainerLow,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Checklist,
                        contentDescription = "Select Notes",
                        tint = if (isSelectionMode) MatrixColors.Primary else MatrixColors.TextSecondary
                    )
                }
            }

            // ── Note Count & Selection Info Header ───────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "${notes.size} ${if (notes.size == 1) "note" else "notes"}",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MatrixColors.TextSecondary
                    )
                )

                if (isSelectionMode) {
                    Text(
                        text = "Markdown notes only",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MatrixColors.Tertiary,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // ── Notes List / Empty State ────────────────────────────────
            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            modifier = Modifier.size(64.dp)
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching notes found" else "No notes yet",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MatrixColors.TextHeader
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "Try adjusting your search terms"
                            } else {
                                "Tap + to create your first note"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixColors.TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = if (isSelectionMode) 120.dp else 88.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        val isSelected = selectedNoteIds.contains(note.id)
                        NoteCard(
                            note = note,
                            onClick = { notesViewModel.openNoteForEdit(note) },
                            onDelete = { notesViewModel.deleteNote(note) },
                            onTogglePin = { notesViewModel.togglePin(note) },
                            isSelectionMode = isSelectionMode,
                            isSelected = isSelected,
                            onToggleSelect = {
                                if (note.format == NoteFormat.MD) {
                                    if (isSelected) selectedNoteIds.remove(note.id)
                                    else selectedNoteIds.add(note.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Selection Action Bottom Floating Bar
        if (isSelectionMode) {
            Surface(
                color = MatrixColors.SurfaceContainerHigh,
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedNoteIds.size} Selected",
                            fontWeight = FontWeight.Bold,
                            color = MatrixColors.TextHeader,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Min 2, Max 50 .md notes",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            batchGraphTargetIds = selectedNoteIds.toSet()
                        },
                        enabled = selectedNoteIds.size >= 2 && selectedNoteIds.size <= 50,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = MatrixColors.OnPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Generate Graph", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        } else {
            // Floating Action Button
            FloatingActionButton(
                onClick = { notesViewModel.createNewNote() },
                containerColor = MatrixColors.Primary,
                contentColor = Color.White,
                shape = MatrixShapes.Xl,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Note")
            }
        }
    }
}
