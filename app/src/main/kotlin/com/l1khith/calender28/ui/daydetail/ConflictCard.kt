package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.domain.model.FreeSlot
import com.l1khith.calender28.ui.conflicts.SlotSuggestionChip
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Interactive card representing a scheduling conflict with auto-suggested slots,
 * move actions, merge, delete, and dismiss capabilities.
 */
@Composable
fun ConflictCard(
    conflict: DayConflict,
    suggestedSlots: List<FreeSlot>,
    onMoveEvent: (eventId: String, conflictEventId: String, isEventA: Boolean, newDateStr: String, newStartTime: String) -> Unit,
    onDeleteEvent: (eventId: String, conflictEventId: String, isEventA: Boolean) -> Unit,
    onMergeEvents: (eventAId: String, eventBId: String) -> Unit,
    onAddBuffer: (eventId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeColor) = when (conflict) {
        is DayConflict.HardOverlap -> "HARD OVERLAP" to MatrixColors.Error
        is DayConflict.CrossDayOverlap -> "CROSS-DAY OVERLAP" to MatrixColors.Error
        is DayConflict.BackToBack -> "BACK-TO-BACK" to Color(0xFFF59E0B)
        is DayConflict.SameSlotStack -> "SAME-SLOT STACK" to Color(0xFFFBBF24)
        is DayConflict.AllDayVsTimed -> "ALL-DAY & TIMED" to MatrixColors.Primary
    }

    var showMoreMenu by remember { mutableStateOf(false) }

    val eventAId = conflict.primaryEventId
    val eventBId = when (conflict) {
        is DayConflict.HardOverlap -> conflict.secondaryEventId
        is DayConflict.CrossDayOverlap -> conflict.crossDayEventId
        is DayConflict.BackToBack -> conflict.secondaryEventId
        is DayConflict.AllDayVsTimed -> conflict.allDayEventId
        is DayConflict.SameSlotStack -> conflict.eventIds.getOrNull(1) ?: ""
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = typeTitle,
                    color = typeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions",
                            tint = MatrixColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        if (eventAId.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Delete Event A", color = MatrixColors.Error) },
                                onClick = {
                                    showMoreMenu = false
                                    onDeleteEvent(eventAId, eventBId, true)
                                }
                            )
                        }
                        if (eventBId.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Delete Event B", color = MatrixColors.Error) },
                                onClick = {
                                    showMoreMenu = false
                                    onDeleteEvent(eventBId, eventAId, false)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (conflict) {
                is DayConflict.HardOverlap -> {
                    Text(
                        text = "• ${conflict.primaryTitle}\n• ${conflict.secondaryTitle}",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Events share an overlapping time block.",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                is DayConflict.CrossDayOverlap -> {
                    Text(
                        text = "• ${conflict.primaryTitle}\n• ${conflict.crossDayTitle} (Spans midnight)",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                is DayConflict.BackToBack -> {
                    Text(
                        text = "• ${conflict.primaryTitle} ends as ${conflict.secondaryTitle} starts",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Zero buffer between events.",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                is DayConflict.SameSlotStack -> {
                    Text(
                        text = "${conflict.eventIds.size} events stacked within 30 min window",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                is DayConflict.AllDayVsTimed -> {
                    Text(
                        text = "Timed event '${conflict.timedTitle}' coincides with all-day '${conflict.allDayTitle}'",
                        color = MatrixColors.TextHeader,
                        fontSize = 13.sp
                    )
                }
            }

            // Auto-suggested free slots chips
            if (suggestedSlots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Nearest conflict-free slots:",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    suggestedSlots.take(3).forEach { slot ->
                        SlotSuggestionChip(
                            slot = slot,
                            onClick = {
                                val targetId = if (eventBId.isNotEmpty()) eventBId else eventAId
                                onMoveEvent(targetId, eventAId, targetId == eventAId, slot.dateStr, slot.startTime)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary action resolution buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Move action
                if (suggestedSlots.isNotEmpty()) {
                    val firstSlot = suggestedSlots.first()
                    Button(
                        onClick = {
                            val targetId = if (eventBId.isNotEmpty()) eventBId else eventAId
                            onMoveEvent(targetId, eventAId, targetId == eventAId, firstSlot.dateStr, firstSlot.startTime)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Move → ${firstSlot.startTime}", fontSize = 11.sp, color = MatrixColors.OnPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                // Merge action (for hard overlap)
                if (conflict is DayConflict.HardOverlap && eventAId.isNotEmpty() && eventBId.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onMergeEvents(eventAId, eventBId) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Merge", fontSize = 11.sp, color = MatrixColors.Primary, fontWeight = FontWeight.Bold)
                    }
                }

                if (conflict is DayConflict.BackToBack && eventBId.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { onAddBuffer(eventBId) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+15m Buffer", fontSize = 11.sp)
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Ignore", fontSize = 11.sp, color = MatrixColors.TextSecondary)
                }
            }
        }
    }
}
