package com.l1khith.calender28.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Obsidian/Apple Notes-style minimalist infinite writing canvas.
 * - No title field: note content is the single source of truth.
 * - No nested Scaffold or double insets: crisp header bar with zero dead whitespace.
 * - Book icon toggle for Markdown preview/editor.
 * - Format (TXT / MD) toggled via the top-right three-dot menu.
 * - Zero emojis or hardcoded icons.
 */
@Composable
fun NoteEditorScreen(
    title: String = "",
    content: String,
    format: NoteFormat,
    isExistingNote: Boolean,
    noteForMenu: Note,
    onTitleChange: (String) -> Unit = {},
    onContentChange: (String) -> Unit,
    onToggleFormat: () -> Unit,
    onPreviewClick: () -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    isPro: Boolean = false,
    onUpgrade: () -> Unit = {},
    modifier: Modifier = Modifier
) {
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
                    contentDescription = "Back",
                    tint = MatrixColors.TextHeader
                )
            }

            Text(
                text = if (isExistingNote) "Note" else "New Note",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.TextHeader
                ),
                modifier = Modifier.weight(1f)
            )

            // Book icon button to toggle to Preview mode when in Markdown (Pro only)
            if (format == NoteFormat.MD && isPro) {
                IconButton(onClick = onPreviewClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                        contentDescription = "Preview Note",
                        tint = MatrixColors.TextHeader
                    )
                }
            }

            // Three-dot menu: Save as MD / Save as TXT, Preview, Share, Delete
            NoteEditorMenu(
                note = noteForMenu,
                onToggleFormat = onToggleFormat,
                onPreview = if (format == NoteFormat.MD && isPro) onPreviewClick else null,
                onDeleteConfirmed = if (isExistingNote) onDelete else null,
                isPro = isPro,
                onUpgrade = onUpgrade
            )
        }

        HorizontalDivider(
            color = MatrixColors.OutlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )

        // ── Obsidian-Style Infinite Canvas ──────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.TextHeader,
                    fontSize = 20.sp
                ),
                cursorBrush = SolidColor(MatrixColors.Primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "Title",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.TextSecondary.copy(alpha = 0.4f),
                                    fontSize = 20.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            BasicTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 400.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MatrixColors.TextHeader,
                    fontSize = 16.sp,
                    lineHeight = 26.sp
                ),
                cursorBrush = SolidColor(MatrixColors.Primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (content.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MatrixColors.TextSecondary.copy(alpha = 0.5f),
                                    fontSize = 16.sp,
                                    lineHeight = 26.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
