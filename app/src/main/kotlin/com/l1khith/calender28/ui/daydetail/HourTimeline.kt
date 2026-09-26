package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.domain.model.DayConflict
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.TimeFormatter
import java.util.Calendar

@Composable
fun HourTimeline(
    dateStr: String,
    dayTimelineItems: List<DayTimelineItem>,
    unscheduledItems: List<DayTimelineItem> = emptyList(),
    crossDayTasks: List<AppTask> = emptyList(),
    conflicts: List<DayConflict> = emptyList(),
    isToday: Boolean,
    onTaskClick: (AppTask) -> Unit,
    onToggleTaskComplete: (AppTask) -> Unit,
    onRecurringClick: (RecurringTask, AppTask?) -> Unit = { _, _ -> },
    onToggleRecurringComplete: (RecurringTask, AppTask?, Boolean) -> Unit = { _, _, _ -> },
    onHabitClick: (Habit) -> Unit = {},
    onToggleHabitComplete: (Habit) -> Unit = {},
    onCreateTaskAtHour: (Int) -> Unit,
    onNoteClick: (Note) -> Unit = {},
    modifier: Modifier = Modifier,
    hourRowHeight: Dp = 64.dp
) {
    val context = LocalContext.current
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val initialScrollHour = if (isToday) maxOf(0, currentHour - 1) else 8

    val initialIndex = remember(dayTimelineItems, initialScrollHour) {
        val idx = dayTimelineItems.indexOfFirst { it is DayTimelineItem.HourHeader && it.hour == initialScrollHour }
        if (idx >= 0) idx else 0
    }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    // Continuation banners for tasks entering or exiting this day
    val continuesFrom = crossDayTasks.filter { it.associatedDate < dateStr }
    val continuesTo = crossDayTasks.filter { (it.endDate ?: it.associatedDate) > dateStr }

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

        // ── Top Unscheduled Section ──
        if (unscheduledItems.isNotEmpty()) {
            item(key = "unscheduled_items_section") {
                CollapsibleGroup(
                    title = "📋 Unscheduled Items (${unscheduledItems.size})",
                    defaultExpanded = false,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        unscheduledItems.forEach { item ->
                            when (item) {
                                is DayTimelineItem.OneOffTask -> {
                                    TimeBlockItem(
                                        task = item.task,
                                        onClick = { onTaskClick(item.task) },
                                        onToggleComplete = { onToggleTaskComplete(item.task) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    )
                                }
                                is DayTimelineItem.RecurringTaskInstance -> {
                                    RecurringTimelineItem(
                                        recurringTask = item.task,
                                        isCompleted = item.isCompleted,
                                        onClick = { onRecurringClick(item.task, item.generatedTask) },
                                        onToggleComplete = {
                                            onToggleRecurringComplete(item.task, item.generatedTask, item.isCompleted)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                    )
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }

        // ── 24-Hour Unified Timeline Rows ──
        val effectiveTimelineItems = if (dayTimelineItems.isNotEmpty()) {
            dayTimelineItems
        } else {
            (0..23).map { DayTimelineItem.HourHeader(it) }
        }

        items(
            items = effectiveTimelineItems,
            key = { item ->
                when (item) {
                    is DayTimelineItem.HourHeader -> "hour_${item.hour}"
                    is DayTimelineItem.OneOffTask -> "task_${item.task.id}"
                    is DayTimelineItem.RecurringTaskInstance -> "rec_${item.task.id}_${item.generatedTask?.id ?: item.timeMinutes ?: "tmpl"}"
                    is DayTimelineItem.HabitReminder -> "habit_${item.habit.id}"
                    is DayTimelineItem.NoteItem -> "note_${item.note.id}"
                }
            }
        ) { item ->
            when (item) {
                is DayTimelineItem.HourHeader -> {
                    val hour = item.hour
                    val timeLabel = TimeFormatter.formatHourHeader(context, hour)

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
                            // Time Label (Left axis, formatted per 24h / 12h)
                            Box(
                                modifier = Modifier
                                    .width(64.dp)
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

                            // Clickable empty slot area for creating a task
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .padding(end = 12.dp)
                            )
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

                is DayTimelineItem.OneOffTask -> {
                    val isHard = hardConflictTaskIds.contains(item.task.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(64.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MatrixColors.OutlineVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TimeBlockItem(
                            task = item.task,
                            onClick = { onTaskClick(item.task) },
                            onToggleComplete = { onToggleTaskComplete(item.task) },
                            conflictBadgeType = if (isHard) ConflictBadgeType.HARD else null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 12.dp)
                        )
                    }
                }

                is DayTimelineItem.RecurringTaskInstance -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(64.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MatrixColors.OutlineVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        RecurringTimelineItem(
                            recurringTask = item.task,
                            isCompleted = item.isCompleted,
                            onClick = { onRecurringClick(item.task, item.generatedTask) },
                            onToggleComplete = {
                                onToggleRecurringComplete(item.task, item.generatedTask, item.isCompleted)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 12.dp)
                        )
                    }
                }

                is DayTimelineItem.HabitReminder -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.width(64.dp))
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MatrixColors.OutlineVariant.copy(alpha = 0.5f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        HabitTimelineItem(
                            habit = item.habit,
                            isCompleted = item.isCompleted,
                            onClick = { onHabitClick(item.habit) },
                            onToggleComplete = { onToggleHabitComplete(item.habit) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(end = 12.dp)
                        )
                    }
                }

                is DayTimelineItem.NoteItem -> {
                    NoteTimelineItem(
                        note = item.note,
                        onClick = { onNoteClick(item.note) },
                        modifier = Modifier.fillMaxWidth()
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

        // Bottom space so FAB / bottomBar doesn't obscure 23:00
        item(key = "bottom_spacer") {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
