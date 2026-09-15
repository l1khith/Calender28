package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.ConflictResolver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictReviewSheet(
    conflicts: List<DayConflict>,
    dateStr: String,
    allTasks: List<AppTask>,
    onDismiss: () -> Unit,
    onApplySlot: (eventId: String, newTime: String) -> Unit,
    onAddBuffer: (eventId: String) -> Unit,
    onDismissConflict: (conflict: DayConflict) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MatrixColors.Surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚠️ Conflicts in Your Day",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MatrixColors.TextHeader
                    )
                )
                TextButton(onClick = onDismiss) {
                    Text("Close", color = MatrixColors.Primary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (conflicts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎉 All conflicts resolved!",
                        color = MatrixColors.TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(conflicts, key = { "${it.primaryEventId}_${it.severity}" }) { conflict ->
                        val candidateDuration = when (conflict) {
                            is DayConflict.HardOverlap -> 60L
                            is DayConflict.CrossDayOverlap -> 60L
                            else -> 30L
                        }
                        val suggestedSlot = ConflictResolver.findConflictFreeSlot(
                            candidateDate = dateStr,
                            durationMinutes = candidateDuration,
                            existingTasks = allTasks,
                            ignoreTaskId = conflict.primaryEventId
                        )

                        ConflictCard(
                            conflict = conflict,
                            suggestedSlot = suggestedSlot,
                            onApplySlot = onApplySlot,
                            onAddBuffer = onAddBuffer,
                            onDismiss = { onDismissConflict(conflict) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
