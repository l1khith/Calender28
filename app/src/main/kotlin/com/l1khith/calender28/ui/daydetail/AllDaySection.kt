package com.l1khith.calender28.ui.daydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun AuxiliarySections(
    allDayTasks: List<AppTask>,
    recurringTasks: List<RecurringTask>,
    habits: List<Habit>,
    focusSessions: List<FocusSession>,
    onTaskClick: (AppTask) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (allDayTasks.isNotEmpty()) {
            CollapsibleGroup(
                title = "🌅 All-Day Events (${allDayTasks.size})",
                defaultExpanded = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    allDayTasks.forEach { task ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.Primary.copy(alpha = 0.12f))
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
                                    text = "All Day",
                                    color = MatrixColors.Primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (recurringTasks.isNotEmpty()) {
            CollapsibleGroup(
                title = "🔁 Recurring Routines (${recurringTasks.size})",
                defaultExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    recurringTasks.forEach { rec ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.SurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rec.title,
                                    color = MatrixColors.TextHeader,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = rec.reminderTime ?: "Routine",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (habits.isNotEmpty()) {
            CollapsibleGroup(
                title = "🔥 Habits Active (${habits.size})",
                defaultExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    habits.forEach { habit ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.SurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = habit.name,
                                    color = MatrixColors.TextHeader,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = habit.reminderTime ?: "Daily",
                                    color = MatrixColors.Tertiary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (focusSessions.isNotEmpty()) {
            CollapsibleGroup(
                title = "⏱️ Focus Sessions Today (${focusSessions.size})",
                defaultExpanded = false
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    focusSessions.forEach { session ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.SurfaceContainerLow)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = session.taskTitle.ifEmpty { "Focus" },
                                    color = MatrixColors.TextHeader,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = session.formattedDuration,
                                    color = MatrixColors.Secondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsibleGroup(
    title: String,
    defaultExpanded: Boolean,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = MatrixColors.TextSecondary
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    content()
                }
            }
        }
    }
}
