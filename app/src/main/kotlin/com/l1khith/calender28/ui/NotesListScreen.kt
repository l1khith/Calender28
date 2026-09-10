package com.l1khith.calender28.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.l1khith.calender28.ui.notes.NoteCard
import com.l1khith.calender28.ui.notes.NoteEditorScreen
import com.l1khith.calender28.ui.notes.NotePreviewScreen
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
    val draftContent by notesViewModel.draftContent.collectAsStateWithLifecycle()
    val draftFormat by notesViewModel.draftFormat.collectAsStateWithLifecycle()
    val isPreviewMode by notesViewModel.isPreviewMode.collectAsStateWithLifecycle()

    // Trigger creating note from external triggers if updated
    LaunchedEffect(createNoteTrigger) {
        if (createNoteTrigger > 0) {
            notesViewModel.createNewNote()
        }
    }

    // Handle Predictive Back / Android Back Press (auto-saves note)
    BackHandler(enabled = activeNote != null) {
        if (isPreviewMode) {
            notesViewModel.setPreviewMode(false)
        } else {
            notesViewModel.saveAndCloseEditor()
        }
    }

    // ─── 1. Sub-Screen: Note Preview Screen ──────────────────────────────
    if (activeNote != null && isPreviewMode) {
        val previewDraft = activeNote!!.copy(
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
            content = draftContent,
            format = draftFormat
        )
        NoteEditorScreen(
            content = draftContent,
            format = draftFormat,
            isExistingNote = isExisting,
            noteForMenu = noteForMenu,
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

    // ─── 3. Main Screen: Notes List Screen (No nested Scaffold to prevent double insets) ─
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
            // ── Search Bar ──────────────────────────────────────────────
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // ── Note Count Header (Zero Emojis) ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
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
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(items = notes, key = { it.id }) { note ->
                        NoteCard(
                            note = note,
                            onClick = { notesViewModel.openNoteForEdit(note) },
                            onDelete = { notesViewModel.deleteNote(note) },
                            onTogglePin = { notesViewModel.togglePin(note) }
                        )
                    }
                }
            }
        }

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
