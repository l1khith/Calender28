package com.l1khith.calender28.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.currentTimeMillis
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel

private const val TAG = "TasksScreen"

@Composable
fun TasksScreen(
    viewModel: FixedCalendarViewModel,
    onEditTask: (AppTask) -> Unit,
    isProActive: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    onStartFocus: (AppTask) -> Unit = {}
) {
    val allTasks by viewModel.allTasks.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val todayFixed = remember { FixedCalendarHelper.fromTimestamp(currentTimeMillis()) }
    val todayDateStr = todayFixed.toString()

    LaunchedEffect(allTasks.size) {
        Log.d(TAG, "TasksScreen composition: total allTasks count = ${allTasks.size}")
    }

    val filteredTasks = remember(allTasks, searchQuery) {
        if (searchQuery.isBlank()) {
            allTasks
        } else {
            allTasks.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        (it.description?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
    }

    val todaysFocusIcon = remember {
        ImageVector.Builder(
            name = "TodaysFocusIcon",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M10 20C8.61667 20 7.31667 19.7375 6.1 19.2125C4.88333 18.6875 3.825 17.975 2.925 17.075C2.025 16.175 1.3125 15.1167 0.7875 13.9C0.2625 12.6833 0 11.3833 0 10C0 8.61667 0.2625 7.31667 0.7875 6.1C1.3125 4.88333 2.025 3.825 2.925 2.925C3.825 2.025 4.88333 1.3125 6.1 0.7875C7.31667 0.2625 8.61667 0 10 0C11.3833 0 12.6833 0.2625 13.9 0.7875C15.1167 1.3125 16.175 2.025 17.075 2.925C17.975 3.825 18.6875 4.88333 19.2125 6.1C19.7375 7.31667 20 8.61667 20 10C20 11.3833 19.7375 12.6833 19.2125 13.9C18.6875 15.1167 17.975 16.175 17.075 17.075C16.175 17.975 15.1167 18.6875 13.9 19.2125C12.6833 19.7375 11.3833 20 10 20ZM10 18C12.2333 18 14.125 17.225 15.675 15.675C17.225 14.125 18 12.2333 18 10C18 7.76667 17.225 5.875 15.675 4.325C14.125 2.775 12.2333 2 10 2C7.76667 2 5.875 2.775 4.325 4.325C2.775 5.875 2 7.76667 2 10C2 12.2333 2.775 14.125 4.325 15.675C5.875 17.225 7.76667 18 10 18ZM10 16C8.33333 16 6.91667 15.4167 5.75 14.25C4.58333 13.0833 4 11.6667 4 10C4 8.33333 4.58333 6.91667 5.75 5.75C6.91667 4.58333 8.33333 4 10 4C11.6667 4 13.0833 4.58333 14.25 5.75C15.4167 6.91667 16 8.33333 16 10C16 11.6667 15.4167 13.0833 14.25 14.25C13.0833 15.4167 11.6667 16 10 16ZM10 14C11.1 14 12.0417 13.6083 12.825 12.825C13.6083 12.0417 14 11.1 14 10C14 8.9 13.6083 7.95833 12.825 7.175C12.0417 6.39167 11.1 6 10 6C8.9 6 7.95833 6.39167 7.175 7.175C6.39167 7.95833 6 8.9 6 10C6 11.1 6.39167 12.0417 7.175 12.825C7.95833 13.6083 8.9 14 10 14ZM10 12C9.45 12 8.97917 11.8042 8.5875 11.4125C8.19583 11.0208 8 10.55 8 10C8 9.45 8.19583 8.97917 8.5875 8.5875C8.97917 8.19583 9.45 8 10 8C10.55 8 11.0208 8.19583 11.4125 8.5875C11.8042 8.97917 12 9.45 12 10C12 10.55 11.8042 11.0208 11.4125 11.4125C11.0208 11.8042 10.55 12 10 12Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFFADC6FF))
        ).build()
    }

    val upcomingHeaderIcon = remember {
        ImageVector.Builder(
            name = "UpcomingHeaderIcon",
            defaultWidth = 18.dp,
            defaultHeight = 20.dp,
            viewportWidth = 18f,
            viewportHeight = 20f
        ).addPath(
            pathData = PathParser().parsePathString(
                "M9 12C8.71667 12 8.47917 11.9042 8.2875 11.7125C8.09583 11.5208 8 11.2833 8 11C8 10.7167 8.09583 10.4792 8.2875 10.2875C8.47917 10.0958 8.71667 10 9 10C9.28333 10 9.52083 10.0958 9.7125 10.2875C9.90417 10.4792 10 10.7167 10 11C10 11.2833 9.90417 11.5208 9.7125 11.7125C9.52083 11.9042 9.28333 12 9 12ZM5 12C4.71667 12 4.47917 11.9042 4.2875 11.7125C4.09583 11.5208 4 11.2833 4 11C4 10.7167 4.09583 10.4792 4.2875 10.2875C4.47917 10.0958 4.71667 10 5 10C5.28333 10 5.52083 10.0958 5.7125 10.2875C5.90417 10.4792 6 10.7167 6 11C6 11.2833 5.90417 11.5208 5.7125 11.7125C5.52083 11.9042 5.28333 12 5 12ZM13 12C12.7167 12 12.4792 11.9042 12.2875 11.7125C12.0958 11.5208 12 11.2833 12 11C12 10.7167 12.0958 10.4792 12.2875 10.2875C12.4792 10.0958 12.7167 10 13 10C13.2833 10 13.5208 10.0958 13.7125 10.2875C13.9042 10.4792 14 10.7167 14 11C14 11.2833 13.9042 11.5208 13.7125 11.7125C13.5208 11.9042 13.2833 12 13 12ZM9 16C8.71667 16 8.47917 15.9042 8.2875 15.7125C8.09583 15.5208 8 15.2833 8 15C8 14.7167 8.09583 14.4792 8.2875 14.2875C8.47917 14.0958 8.71667 14 9 14C9.28333 14 9.52083 14.0958 9.7125 14.2875C9.90417 14.4792 10 14.7167 10 15C10 15.2833 9.90417 15.5208 9.7125 15.7125C9.52083 15.9042 9.28333 16 9 16ZM5 16C4.71667 16 4.47917 15.9042 4.2875 15.7125C4.09583 15.5208 4 15.2833 4 15C4 14.7167 4.09583 14.4792 4.2875 14.2875C4.47917 14.0958 4.71667 14 5 14C5.28333 14 5.52083 10.0958 5.7125 14.2875C5.90417 14.4792 6 14.7167 6 15C6 15.2833 5.90417 15.5208 5.7125 15.7125C5.52083 15.9042 5.28333 16 5 16ZM13 16C12.7167 16 12.4792 15.9042 12.2875 15.7125C12.0958 15.5208 12 15.2833 12 15C12 14.7167 12.0958 14.4792 12.2875 14.2875C12.4792 14.0958 12.7167 10 13 10C13.2833 10 13.5208 10.0958 13.7125 10.2875C13.9042 10.4792 14 10.7167 14 11C14 11.2833 13.9042 11.5208 13.7125 11.7125C13.5208 11.9042 13.2833 16 13 16ZM2 20C1.45 20 0.979167 19.8042 0.5875 19.4125C0.195833 19.0208 0 18.55 0 18V4C0 3.45 0.195833 2.97917 0.5875 2.5875C0.979167 2.19583 1.45 2 2 2H3V0H5V2H13V0H15V2H16C16.55 2 17.0208 2.19583 17.4125 2.5875C17.8042 2.97917 18 3.45 18 4V18C18 18.55 17.8042 19.0208 17.4125 19.4125C17.0208 19.8042 16.55 20 16 20H2ZM2 18H16V8H2V18Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF8C909F))
        ).build()
    }

    val urgentAndOverdue = remember(filteredTasks, todayDateStr) {
        filteredTasks.filter { task ->
            if (task.completed) return@filter false
            if (task.isGenerated == 1) return@filter false
            if (task.isReminder == 0) return@filter false

            val isOverdue = task.associatedDate < todayDateStr
            if (isOverdue) {
                val diffDays = FixedCalendarHelper.daysBetween(task.associatedDate, todayDateStr)
                diffDays <= 7
            } else {
                task.isReminder == 1
            }
        }
    }

    val todaysFocus = remember(filteredTasks, todayDateStr) {
        filteredTasks.filter { task ->
            task.associatedDate == todayDateStr && (task.isReminder == 0 && task.priority < 3)
        }
    }

    val upcomingTasksByDate = remember(filteredTasks, todayDateStr) {
        filteredTasks.filter { task ->
            if (task.completed) return@filter false
            if (task.isGenerated == 1) return@filter false
            val diffDays = FixedCalendarHelper.daysBetween(todayDateStr, task.associatedDate)
            diffDays in 1..2
        }.groupBy { it.associatedDate }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search tasks...",
                    color = MatrixColors.TextSecondary,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MatrixColors.Outline
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MatrixColors.Outline
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MatrixColors.SurfaceContainerLow,
                unfocusedContainerColor = MatrixColors.SurfaceContainerLow,
                focusedBorderColor = MatrixColors.Primary,
                unfocusedBorderColor = MatrixColors.OutlineVariant,
                focusedTextColor = MatrixColors.TextHeader,
                unfocusedTextColor = MatrixColors.TextHeader
            ),
            shape = MatrixShapes.Md,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Section 1: Urgent & Overdue
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgent & Overdue",
                            tint = Color(0xFFFFB4AB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Urgent & Overdue",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    if (urgentAndOverdue.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                            shape = MatrixShapes.Md,
                            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "No urgent or overdue tasks.",
                                color = MatrixColors.TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        for (taskItem in urgentAndOverdue) {
                            UrgentTaskCard(
                                task = taskItem,
                                todayDateStr = todayDateStr,
                                onToggleComplete = { viewModel.toggleTaskCompletion(taskItem) },
                                onClick = { onEditTask(taskItem) },
                                onDeleteTask = { viewModel.deleteTask(taskItem) },
                                onStartFocus = { onStartFocus(taskItem) }
                            )
                        }
                    }
                }
            }

            // Section 2: Today's Focus
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = todaysFocusIcon,
                            contentDescription = "Today's Focus",
                            tint = Color(0xFFADC6FF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Today's Focus",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    if (todaysFocus.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                            shape = MatrixShapes.Md,
                            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "No tasks focus for today.",
                                color = MatrixColors.TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        for (taskItem in todaysFocus) {
                            FocusTaskCard(
                                task = taskItem,
                                onToggleComplete = { viewModel.toggleTaskCompletion(taskItem) },
                                onClick = { onEditTask(taskItem) },
                                onDeleteTask = { viewModel.deleteTask(taskItem) },
                                onStartFocus = { onStartFocus(taskItem) }
                            )
                        }
                    }
                }
            }

            // Section 3: Upcoming
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = upcomingHeaderIcon,
                            contentDescription = "Upcoming",
                            tint = Color(0xFF8C909F),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upcoming",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    if (upcomingTasksByDate.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                            shape = MatrixShapes.Md,
                            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "No upcoming tasks scheduled.",
                                color = MatrixColors.TextSecondary,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        val sortedEntries = upcomingTasksByDate.entries.sortedBy { it.key }
                        for (entry in sortedEntries) {
                            val dateStr = entry.key
                            val dateTasks = entry.value
                            val parsedDate = FixedCalendarHelper.parseDateStr(dateStr)
                            val formattedLabel = if (parsedDate != null) {
                                "${FixedCalendarHelper.getMonthName(parsedDate.month)} ${parsedDate.day}"
                            } else dateStr

                            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                Text(
                                    text = formattedLabel,
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )

                                for (taskItem in dateTasks) {
                                    UpcomingTaskCard(
                                        task = taskItem,
                                        onToggleComplete = { viewModel.toggleTaskCompletion(taskItem) },
                                        onClick = { onEditTask(taskItem) },
                                        onDeleteTask = { viewModel.deleteTask(taskItem) },
                                        onStartFocus = { onStartFocus(taskItem) }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UrgentTaskCard(
    task: AppTask,
    todayDateStr: String,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDeleteTask: () -> Unit = {},
    onStartFocus: () -> Unit = {}
) {
    val isOverdue = task.associatedDate < todayDateStr
    val cardBg = if (isOverdue) Color(0xFF3B2527) else MatrixColors.SurfaceContainerLow
    val borderCol = if (isOverdue) Color(0xFF8C4A4D) else MatrixColors.OutlineVariant
    val badgeBg = if (isOverdue) Color(0xFF93000A) else MatrixColors.Primary
    val badgeTextCol = if (isOverdue) Color(0xFFFFDAD6) else MatrixColors.OnPrimary

    val badgeLabel = if (isOverdue) {
        val diffDays = FixedCalendarHelper.daysBetween(task.associatedDate, todayDateStr)
        "OVERDUE by $diffDays day${if (diffDays > 1) "s" else ""}"
    } else {
        "URGENT"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = MatrixShapes.Md,
        border = BorderStroke(1.dp, borderCol),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        if (task.completed) MatrixColors.Primary else MatrixColors.Outline,
                        CircleShape
                    )
                    .background(
                        if (task.completed) MatrixColors.Primary else Color.Transparent
                    )
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = MatrixColors.OnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(MatrixShapes.Sm)
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeLabel,
                            color = badgeTextCol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (task.reminderTime != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.reminderTime,
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                if (!task.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                if (task.hasEverFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⭐ Focused: ${task.formattedFocusDuration}",
                        color = MatrixColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("⏱️", fontSize = 14.sp)
                }

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MatrixColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FocusTaskCard(
    task: AppTask,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDeleteTask: () -> Unit = {},
    onStartFocus: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        shape = MatrixShapes.Md,
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        if (task.completed) MatrixColors.Primary else MatrixColors.Outline,
                        CircleShape
                    )
                    .background(
                        if (task.completed) MatrixColors.Primary else Color.Transparent
                    )
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = MatrixColors.OnPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                if (!task.description.isNullOrEmpty() || task.reminderTime != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (task.reminderTime != null) {
                            Text(
                                text = task.reminderTime,
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp
                            )
                            if (!task.description.isNullOrEmpty()) {
                                Text(
                                    text = " • ",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (!task.description.isNullOrEmpty()) {
                            Text(
                                text = task.description,
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (task.hasEverFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⭐ Focused: ${task.formattedFocusDuration}",
                        color = MatrixColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.size(28.dp)
                ) {
                    Text("⏱️", fontSize = 14.sp)
                }

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MatrixColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingTaskCard(
    task: AppTask,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDeleteTask: () -> Unit = {},
    onStartFocus: () -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        shape = MatrixShapes.Md,
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        if (task.completed) MatrixColors.Primary else MatrixColors.Outline,
                        CircleShape
                    )
                    .background(
                        if (task.completed) MatrixColors.Primary else Color.Transparent
                    )
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Complete",
                        tint = MatrixColors.OnPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                )

                if (task.reminderTime != null) {
                    Text(
                        text = task.reminderTime,
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp
                    )
                }

                if (task.hasEverFocused) {
                    Text(
                        text = "⭐ Focused: ${task.formattedFocusDuration}",
                        color = MatrixColors.Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onStartFocus,
                    modifier = Modifier.size(26.dp)
                ) {
                    Text("⏱️", fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDeleteTask,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Task",
                        tint = MatrixColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
