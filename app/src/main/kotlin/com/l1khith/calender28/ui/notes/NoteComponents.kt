package com.l1khith.calender28.ui.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import java.text.SimpleDateFormat
import java.util.*

/**
 * Three-dot dropdown menu for Note editor screen.
 * Supports toggling format (TXT <-> MD), previewing markdown with book icon, sharing, and deleting.
 */
@Composable
fun NoteEditorMenu(
    note: Note,
    onToggleFormat: () -> Unit,
    onPreview: (() -> Unit)? = null,
    onDeleteConfirmed: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MatrixColors.TextHeader
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(MatrixColors.SurfaceContainerHigh)
        ) {
            // Toggle Format: Save as MD / Save as TXT
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (note.format == NoteFormat.TXT) "Save as Markdown (.md)" else "Save as Plain Text (.txt)",
                        color = MatrixColors.TextHeader
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (note.format == NoteFormat.TXT) Icons.Default.Code else Icons.Default.Description,
                        contentDescription = "Change Format",
                        tint = MatrixColors.Primary
                    )
                },
                onClick = {
                    menuExpanded = false
                    onToggleFormat()
                }
            )

            // Preview Markdown (if MD) - uses book icon instead of eye
            if (note.format == NoteFormat.MD && onPreview != null) {
                DropdownMenuItem(
                    text = { Text("Preview Markdown", color = MatrixColors.TextHeader) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = "Preview",
                            tint = MatrixColors.Primary
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onPreview()
                    }
                )
            }

            // Share Note
            DropdownMenuItem(
                text = { Text("Share", color = MatrixColors.TextHeader) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MatrixColors.Primary
                    )
                },
                onClick = {
                    menuExpanded = false
                    NoteShareHelper.shareNote(context, note)
                }
            )

            // Delete Note (if existing)
            if (onDeleteConfirmed != null) {
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }

    if (showDeleteConfirm && onDeleteConfirmed != null) {
        DeleteNoteConfirmationDialog(
            noteTitle = note.displayTitle,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/**
 * Three-dot dropdown menu for NotePreviewScreen.
 */
@Composable
fun NotePreviewMenu(
    note: Note,
    onEdit: () -> Unit,
    onDeleteConfirmed: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MatrixColors.TextHeader
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(MatrixColors.SurfaceContainerHigh)
        ) {
            DropdownMenuItem(
                text = { Text("Edit Note", color = MatrixColors.TextHeader) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MatrixColors.Primary
                    )
                },
                onClick = {
                    menuExpanded = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Share", color = MatrixColors.TextHeader) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MatrixColors.Primary
                    )
                },
                onClick = {
                    menuExpanded = false
                    NoteShareHelper.shareNote(context, note)
                }
            )
            if (onDeleteConfirmed != null) {
                DropdownMenuItem(
                    text = { Text("Delete", color = Color(0xFFEF4444)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444)
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }

    if (showDeleteConfirm && onDeleteConfirmed != null) {
        DeleteNoteConfirmationDialog(
            noteTitle = note.displayTitle,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/**
 * Three-dot dropdown menu for NoteCard in list view.
 */
@Composable
fun NoteCardMenu(
    note: Note,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { menuExpanded = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Note Options",
                tint = MatrixColors.TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
            modifier = Modifier.background(MatrixColors.SurfaceContainerHigh)
        ) {
            DropdownMenuItem(
                text = { Text("Share", color = MatrixColors.TextHeader) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = MatrixColors.Primary
                    )
                },
                onClick = {
                    menuExpanded = false
                    NoteShareHelper.shareNote(context, note)
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = Color(0xFFEF4444)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444)
                    )
                },
                onClick = {
                    menuExpanded = false
                    showDeleteConfirm = true
                }
            )
        }
    }

    if (showDeleteConfirm) {
        DeleteNoteConfirmationDialog(
            noteTitle = note.displayTitle,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteConfirmed()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/**
 * Native confirmation dialog for note deletion. Strictly follows app theme tokens.
 */
@Composable
fun DeleteNoteConfirmationDialog(
    noteTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Note",
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader
            )
        },
        text = {
            Text(
                text = if (noteTitle.isNotBlank() && noteTitle != "Untitled Note") {
                    "Are you sure you want to delete \"$noteTitle\"? This action cannot be undone."
                } else {
                    "Are you sure you want to delete this note? This action cannot be undone."
                },
                color = MatrixColors.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = MatrixColors.TextSecondary)
            }
        },
        containerColor = MatrixColors.SurfaceContainerHigh,
        shape = MatrixShapes.Lg
    )
}

/**
 * Format badge displaying [MD] tag only when the note is in Markdown format.
 * Plain text (.txt) is the standard default and is not shown on cards.
 */
@Composable
fun NoteFormatBadge(format: NoteFormat) {
    if (format != NoteFormat.MD) return
    val badgeBg = MatrixColors.Primary.copy(alpha = 0.15f)
    val badgeColor = MatrixColors.Primary

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = badgeBg,
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = format.name,
            color = badgeColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * NoteCard component used in the Notes List.
 * Pure content-derived card: title is first line, subtitle is snippet preview.
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    val formattedDate = remember(note.updatedAt) { dateFormatter.format(Date(note.updatedAt)) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(
            1.dp,
            if (note.isPinned) MatrixColors.Primary.copy(alpha = 0.6f) else MatrixColors.OutlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Pin status (if pinned) + Derived Title + Optional MD Badge + Card Menu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "Pinned Note",
                        tint = MatrixColors.Primary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(onClick = onTogglePin)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = note.displayTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MatrixColors.TextHeader
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (note.format == NoteFormat.MD) {
                    Spacer(modifier = Modifier.width(8.dp))
                    NoteFormatBadge(format = note.format)
                }
                Spacer(modifier = Modifier.width(4.dp))
                NoteCardMenu(note = note, onDeleteConfirmed = onDelete)
            }

            // Row 2: Content snippet preview (from note.snippetPreview)
            if (note.snippetPreview.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.snippetPreview,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MatrixColors.TextSecondary,
                        fontSize = 13.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 3: Metadata footer (formatted date + pin toggle)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Updated $formattedDate",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MatrixColors.TextSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Pin Note",
                        tint = if (note.isPinned) MatrixColors.Primary else MatrixColors.TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
