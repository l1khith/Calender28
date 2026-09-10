package com.l1khith.calender28.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes

/**
 * NotePreviewScreen displays formatted Markdown rendering of a note.
 * - No nested Scaffold or double insets: crisp header bar with zero dead whitespace.
 * - Book icon toggle in the top bar to switch right back to Editor.
 * - Floating action button with Edit icon to quickly return to editing.
 * - Options menu with Edit Note, Share, and Delete.
 * - Back arrow returning to NoteEditorScreen.
 * Pure theme compliance and zero emojis.
 */
@Composable
fun NotePreviewScreen(
    note: Note,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
    ) {
        // ── Minimalist Top Action Bar ──────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Editor",
                    tint = MatrixColors.TextHeader
                )
            }

            Text(
                text = "Preview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.TextHeader
                ),
                modifier = Modifier.weight(1f)
            )

            // Book icon button to toggle back to Editor mode
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = "Switch to Editor",
                    tint = MatrixColors.Primary
                )
            }

            // Share Note
            IconButton(onClick = { NoteShareHelper.shareNote(context, note) }) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = MatrixColors.TextHeader
                )
            }

            // Preview options menu
            NotePreviewMenu(
                note = note,
                onEdit = onBack,
                onDeleteConfirmed = onDelete
            )
        }

        HorizontalDivider(
            color = MatrixColors.OutlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )

        // ── Formatted Markdown Content & Quick Edit FAB ───────────
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                if (note.content.isBlank()) {
                    Text(
                        text = "No content to preview.",
                        color = MatrixColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    MarkdownContent(content = note.content)
                }
            }

            // Floating action button to quickly resume editing
            FloatingActionButton(
                onClick = onBack,
                containerColor = MatrixColors.Primary,
                contentColor = Color.White,
                shape = MatrixShapes.Xl,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Note"
                )
            }
        }
    }
}
