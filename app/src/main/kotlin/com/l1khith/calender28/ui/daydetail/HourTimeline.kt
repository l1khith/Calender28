package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import java.util.Calendar

@Composable
fun HourTimeline(
    dateStr: String,
    timedTasks: List<AppTask>,
    crossDayTasks: List<AppTask>,
    conflicts: List<DayConflict>,
    isToday: Boolean,
    onTaskClick: (AppTask) -> Unit,
    onToggleComplete: (AppTask) -> Unit,
    onCreateTaskAtHour: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hourRowHeight: Dp = 64.dp
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val initialScrollHour = if (isToday) maxOf(0, currentHour - 1) else 8

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialScrollHour)

    // Continuation banners for tasks entering or exiting this day
    val continuesFrom = crossDayTasks.filter { it.associatedDate < dateStr }
    val continuesTo = crossDayTasks.filter { (it.endDate ?: it.associatedDate) > dateStr }

    // Map tasks to their primary starting hour (0..23)
    val tasksByHour = remember(timedTasks) {
        val map = mutableMapOf<Int, MutableList<AppTask>>()
        for (h in 0..23) map[h] = mutableListOf()

        for (task in timedTasks) {
            val time = task.reminderTime
            if (time != null && time.contains(":")) {
                val hour = time.substringBefore(":").toIntOrNull() ?: 0
                if (hour in 0..23) {
                    map[hour]?.add(task)
                }
            } else {
                map[0]?.add(task) // unscheduled goes into hour 0
            }
        }
        map
    }

    // Set of IDs that have hard overlaps
    val hardConflictTaskIds = remember(conflicts) {
        conflicts.filterIsInstance<DayConflict.HardOverlap>()
            .flatMap { listOf(it.primaryEventId, it.secondaryEventId) }
            .toSet()
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 96.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // ── Top Continuation Banners ──
        if (continuesFrom.isNotEmpty()) {
            item(key = "continuation_from") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (task in continuesFrom) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.Secondary.copy(alpha = 0.15f))
                                .clickable { onTaskClick(task) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "▶ Continues from ${task.associatedDate}: ${task.title}",
                                color = MatrixColors.Secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // ── 24-Hour Timeline Rows ──
        items(24, key = { hour -> "hour_row_$hour" }) { hour ->
            val hourTasks = tasksByHour[hour] ?: emptyList()
            val timeLabel = "%02d:00".format(hour)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourRowHeight)
            ) {
                // Background hour divider line
                HorizontalDivider(
                    modifier = Modifier.align(Alignment.TopStart),
                    thickness = if (hour == 0 || hour == 12) 1.5.dp else 0.8.dp,
                    color = MatrixColors.OutlineVariant.copy(alpha = 0.6f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onCreateTaskAtHour(hour) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Label (Left axis)
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight()
                            .padding(start = 12.dp, top = 4.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = timeLabel,
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Vertical subtle axis line
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(MatrixColors.OutlineVariant.copy(alpha = 0.5f))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Events in this hour slot
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(end = 12.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        if (hourTasks.isNotEmpty()) {
                            if (hourTasks.size > 1) {
                                // Side-by-Side Layout for concurrent/overlapping tasks
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    hourTasks.forEach { task ->
                                        val isHard = hardConflictTaskIds.contains(task.id)
                                        TimeBlockItem(
                                            task = task,
                                            onClick = { onTaskClick(task) },
                                            onToggleComplete = { onToggleComplete(task) },
                                            conflictBadgeType = if (isHard) ConflictBadgeType.HARD else null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        )
                                    }
                                }
                            } else {
                                // Single full-width item
                                val task = hourTasks.first()
                                val isHard = hardConflictTaskIds.contains(task.id)
                                TimeBlockItem(
                                    task = task,
                                    onClick = { onTaskClick(task) },
                                    onToggleComplete = { onToggleComplete(task) },
                                    conflictBadgeType = if (isHard) ConflictBadgeType.HARD else null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }

                // Now Indicator line if viewing today and current hour matches
                if (isToday && hour == currentHour) {
                    NowIndicator(
                        hourRowHeight = hourRowHeight,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }
            }
        }

        // ── Bottom Continuation Banners ──
        if (continuesTo.isNotEmpty()) {
            item(key = "continuation_to") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (task in continuesTo) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MatrixColors.Secondary.copy(alpha = 0.15f))
                                .clickable { onTaskClick(task) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "◀ Continues to ${task.endDate}: ${task.title}",
                                color = MatrixColors.Secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Bottom space so FAB doesn't obscure 23:00
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
