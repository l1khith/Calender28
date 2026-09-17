package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun UnscheduledTasksSection(
    tasks: List<AppTask>,
    onTaskClick: (AppTask) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tasks.isEmpty()) return

    CollapsibleGroup(
        title = "📋 Unscheduled Tasks (${tasks.size})",
        defaultExpanded = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            tasks.forEach { task ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MatrixColors.SurfaceContainerLow)
                        .clickable { onTaskClick(task) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "No time set",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
