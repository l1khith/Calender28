package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun ConflictCard(
    conflict: DayConflict,
    suggestedSlot: String?,
    onApplySlot: (eventId: String, newTime: String) -> Unit,
    onAddBuffer: (eventId: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (typeTitle, typeColor) = when (conflict) {
        is DayConflict.HardOverlap -> "🔴 HARD OVERLAP" to MatrixColors.Error
        is DayConflict.CrossDayOverlap -> "🔴 CROSS-DAY OVERLAP" to MatrixColors.Error
        is DayConflict.BackToBack -> "🟠 BACK-TO-BACK" to Color(0xFFF59E0B)
        is DayConflict.SameSlotStack -> "🟡 SAME-SLOT STACK" to Color(0xFFFBBF24)
        is DayConflict.AllDayVsTimed -> "🔵 ALL-DAY & TIMED" to MatrixColors.Primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = typeTitle,
                color = typeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            when (conflict) {
                is DayConflict.HardOverlap -> {
                    Text(
                        text = "• ${conflict.primaryTitle}\n• ${conflict.secondaryTitle}",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Events share the same time slot.",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
                is DayConflict.CrossDayOverlap -> {
                    Text(
                        text = "• ${conflict.primaryTitle}\n• ${conflict.crossDayTitle} (Cross-Day)",
                        color = MatrixColors.TextHeader,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                is DayConflict.BackToBack -> {
                    Text(
                        text = "• ${conflict.primaryTitle} ends as ${conflict.secondaryTitle} begins",
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
                        text = "${conflict.eventIds.size} events stacked within a 30-min window",
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

            Spacer(modifier = Modifier.height(10.dp))

            // Action resolution chips / buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (suggestedSlot != null && (conflict is DayConflict.HardOverlap || conflict is DayConflict.CrossDayOverlap)) {
                    val targetId = when (conflict) {
                        is DayConflict.HardOverlap -> conflict.secondaryEventId
                        is DayConflict.CrossDayOverlap -> conflict.crossDayEventId
                        else -> conflict.primaryEventId
                    }
                    Button(
                        onClick = { onApplySlot(targetId, suggestedSlot) },
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Move → $suggestedSlot", fontSize = 11.sp, color = MatrixColors.OnPrimary)
                    }
                }

                if (conflict is DayConflict.BackToBack) {
                    OutlinedButton(
                        onClick = { onAddBuffer(conflict.secondaryEventId) },
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
