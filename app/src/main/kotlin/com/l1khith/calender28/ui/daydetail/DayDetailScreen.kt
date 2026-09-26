package com.l1khith.calender28.ui.daydetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.l1khith.calender28.data.Note
import com.l1khith.calender28.data.NoteFormat
import com.l1khith.calender28.ui.notes.NoteEditorScreen
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dateStr: String,
    onBack: () -> Unit,
    onOpenCreateTask: (dateStr: String, initialHour: Int?) -> Unit,
    onEditTask: (AppTask) -> Unit,
    onNoteClick: ((Note) -> Unit)? = null,
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
    val dayTimelineItems by viewModel.dayTimelineItems.collectAsStateWithLifecycle()
    val unscheduledItems by viewModel.unscheduledItems.collectAsStateWithLifecycle()
    val isProActive by com.l1khith.calender28.billing.SubscriptionManager.isProActive.collectAsStateWithLifecycle()
    var editingNote by remember { mutableStateOf<Note?>(null) }

    BackHandler(enabled = editingNote != null) {
        editingNote = null
    }

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

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastResolutionResult) {
        val result = uiState.lastResolutionResult
        if (result != null) {
            val snackbarResult = snackbarHostState.showSnackbar(
                message = "Action: ${result.action.name.replace("_", " ")} applied",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                viewModel.undoLastResolution()
            } else {
                viewModel.clearLastResolution()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = MatrixColors.SurfaceContainerHigh,
                        contentColor = MatrixColors.TextHeader,
                        actionColor = MatrixColors.Primary
                    )
                }
            )
        },
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
                    SummaryChip(label = "${uiState.timedTasks.size + uiState.allDayTasks.size + uiState.unscheduledTasks.size} tasks", color = MatrixColors.Primary)
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
        bottomBar = {
            Surface(
                color = MatrixColors.SurfaceContainer,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onOpenCreateTask(uiState.selectedDate.toString(), null) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = MatrixColors.OnPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Task",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (uiState.activeConflicts.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { viewModel.setReviewSheetOpen(true) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MatrixColors.Secondary
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MatrixColors.Secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Resolve (${uiState.activeConflicts.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
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
                    dayTimelineItems = dayTimelineItems,
                    unscheduledItems = unscheduledItems,
                    crossDayTasks = uiState.crossDayTasks,
                    conflicts = uiState.activeConflicts,
                    isToday = isToday,
                    onTaskClick = onEditTask,
                    onToggleTaskComplete = { viewModel.toggleTaskComplete(it) },
                    onRecurringClick = { rec, genTask ->
                        if (genTask != null) onEditTask(genTask)
                    },
                    onToggleRecurringComplete = { rec, genTask, isCompleted ->
                        viewModel.toggleRecurringComplete(rec, genTask, isCompleted)
                    },
                    onHabitClick = { habit ->
                        // Focus or detail interaction
                    },
                    onToggleHabitComplete = { habit ->
                        viewModel.toggleHabitComplete(habit)
                    },
                    onCreateTaskAtHour = { hour ->
                        onOpenCreateTask(uiState.selectedDate.toString(), hour)
                    },
                    onNoteClick = { note ->
                        if (onNoteClick != null) {
                            onNoteClick(note)
                        } else {
                            editingNote = note
                        }
                    }
                )
            }

            // Auxiliary sections at bottom (Focus Sessions)
            if (uiState.focusSessions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    FocusSection(focusSessions = uiState.focusSessions)
                }
            }
        }

        // Conflict Review Bottom Sheet
        if (uiState.isReviewSheetOpen) {
            val combinedTasks = remember(uiState.timedTasks, uiState.crossDayTasks, uiState.allDayTasks, uiState.unscheduledTasks) {
                uiState.timedTasks + uiState.crossDayTasks + uiState.allDayTasks + uiState.unscheduledTasks
            }
            ConflictReviewSheet(
                conflicts = uiState.activeConflicts,
                dateStr = uiState.selectedDate.toString(),
                allTasks = combinedTasks,
                onDismiss = { viewModel.setReviewSheetOpen(false) },
                onMoveEvent = { eventId, conflictEventId, isEventA, newDateStr, newStartTime ->
                    viewModel.moveEvent(eventId, conflictEventId, isEventA, newDateStr, newStartTime)
                },
                onDeleteEvent = { eventId, conflictEventId, isEventA ->
                    viewModel.deleteEvent(eventId, conflictEventId, isEventA)
                },
                onMergeEvents = { eventAId, eventBId ->
                    viewModel.mergeEvents(eventAId, eventBId)
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

    if (editingNote != null) {
        val note = editingNote!!
        var draftTitle by remember(note.id) { mutableStateOf(note.title) }
        var draftContent by remember(note.id) { mutableStateOf(note.content) }
        var draftFormat by remember(note.id) { mutableStateOf(note.format) }
        val noteForMenu = remember(note.id, draftTitle, draftContent, draftFormat) {
            note.copy(title = draftTitle, content = draftContent, format = draftFormat)
        }

        NoteEditorScreen(
            title = draftTitle,
            content = draftContent,
            format = draftFormat,
            isExistingNote = true,
            noteForMenu = noteForMenu,
            onTitleChange = { draftTitle = it },
            onContentChange = { draftContent = it },
            onToggleFormat = {
                draftFormat = if (draftFormat == NoteFormat.TXT) NoteFormat.MD else NoteFormat.TXT
            },
            onPreviewClick = { },
            onBack = {
                viewModel.saveNote(note.copy(title = draftTitle, content = draftContent, format = draftFormat))
                editingNote = null
            },
            onDelete = {
                viewModel.deleteNote(note)
                editingNote = null
            },
            isPro = isProActive,
            onUpgrade = { },
            modifier = Modifier.fillMaxSize()
        )
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
