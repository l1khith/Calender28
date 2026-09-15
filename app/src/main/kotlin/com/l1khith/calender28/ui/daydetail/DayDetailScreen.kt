package com.l1khith.calender28.ui.daydetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dateStr: String,
    onBack: () -> Unit,
    onOpenCreateTask: (dateStr: String, initialHour: Int?) -> Unit,
    onEditTask: (AppTask) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DayDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.l1khith.calender28.viewmodel.AppViewModelProvider.Factory)
) {
    BackHandler(onBack = onBack)

    val fixedDate = remember(dateStr) {
        FixedCalendarHelper.parseDateStr(dateStr) ?: FixedCalendarHelper.currentFixedDate()
    }

    LaunchedEffect(dateStr) {
        viewModel.loadDate(fixedDate)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val todayFixed = remember { FixedCalendarHelper.currentFixedDate() }
    val isToday = remember(uiState.selectedDate) {
        uiState.selectedDate.year == todayFixed.year &&
                uiState.selectedDate.month == todayFixed.month &&
                uiState.selectedDate.day == todayFixed.day
    }

    val dayOfWeek = remember(uiState.selectedDate) {
        FixedCalendarHelper.getDayOfWeek(uiState.selectedDate)
    }
    val weekOfCycle = remember(uiState.selectedDate) {
        ((uiState.selectedDate.day - 1) / 7) + 1
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MatrixColors.Surface)
            ) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "${FixedCalendarHelper.getMonthName(uiState.selectedDate.month)} ${uiState.selectedDate.day}, ${uiState.selectedDate.year}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.TextHeader
                                )
                            )
                            Text(
                                text = "$dayOfWeek · Week $weekOfCycle of Cycle",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixColors.TextSecondary
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MatrixColors.TextHeader
                            )
                        }
                    },
                    actions = {
                        if (!isToday) {
                            IconButton(onClick = { viewModel.loadDate(todayFixed) }) {
                                Icon(
                                    imageVector = Icons.Filled.Today,
                                    contentDescription = "Today",
                                    tint = MatrixColors.Primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MatrixColors.Surface
                    )
                )

                // Summary metric strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryChip(label = "${uiState.timedTasks.size + uiState.allDayTasks.size} tasks", color = MatrixColors.Primary)
                    SummaryChip(label = "${uiState.habits.size} habits", color = MatrixColors.Tertiary)
                    SummaryChip(label = "${uiState.focusSessions.size} focus", color = MatrixColors.Secondary)
                    SummaryChip(
                        label = "%.1fh free".format(uiState.freeHoursEstimated),
                        color = MatrixColors.TextSecondary
                    )
                }

                // Conflict warning banner
                if (uiState.activeConflicts.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ConflictBanner(
                            conflictCount = uiState.activeConflicts.size,
                            onReviewClick = { viewModel.setReviewSheetOpen(true) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onOpenCreateTask(uiState.selectedDate.toString(), null) },
                containerColor = MatrixColors.Primary,
                contentColor = MatrixColors.OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Task")
            }
        },
        containerColor = MatrixColors.Surface,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Hour Timeline
            Box(modifier = Modifier.weight(1f)) {
                HourTimeline(
                    dateStr = uiState.selectedDate.toString(),
                    timedTasks = uiState.timedTasks,
                    crossDayTasks = uiState.crossDayTasks,
                    conflicts = uiState.activeConflicts,
                    isToday = isToday,
                    onTaskClick = onEditTask,
                    onToggleComplete = { viewModel.toggleTaskComplete(it) },
                    onCreateTaskAtHour = { hour ->
                        onOpenCreateTask(uiState.selectedDate.toString(), hour)
                    }
                )
            }

            // Collapsible auxiliary sections at bottom
            if (uiState.allDayTasks.isNotEmpty() || uiState.recurringInstances.isNotEmpty() ||
                uiState.habits.isNotEmpty() || uiState.focusSessions.isNotEmpty()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    AuxiliarySections(
                        allDayTasks = uiState.allDayTasks,
                        recurringTasks = uiState.recurringInstances,
                        habits = uiState.habits,
                        focusSessions = uiState.focusSessions,
                        onTaskClick = onEditTask
                    )
                }
            }
        }

        // Conflict Review Bottom Sheet
        if (uiState.isReviewSheetOpen) {
            val combinedTasks = remember(uiState.timedTasks, uiState.crossDayTasks, uiState.allDayTasks) {
                uiState.timedTasks + uiState.crossDayTasks + uiState.allDayTasks
            }
            ConflictReviewSheet(
                conflicts = uiState.activeConflicts,
                dateStr = uiState.selectedDate.toString(),
                allTasks = combinedTasks,
                onDismiss = { viewModel.setReviewSheetOpen(false) },
                onApplySlot = { eventId, newTime ->
                    viewModel.applyConflictSlot(eventId, newTime)
                },
                onAddBuffer = { eventId ->
                    viewModel.addBuffer(eventId)
                },
                onDismissConflict = { conflict ->
                    viewModel.dismissConflict(conflict)
                }
            )
        }
    }
}

@Composable
private fun SummaryChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
