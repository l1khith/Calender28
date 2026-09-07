package com.l1khith.calender28.ui

import android.content.Context

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.HabitCycleEngine
import com.l1khith.calender28.utils.PlatformTimePicker
import com.l1khith.calender28.utils.currentTimeMillis
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel
import android.util.Log

import androidx.lifecycle.compose.collectAsStateWithLifecycle

private const val TAG = "HabitScreen"

@Composable
fun HabitSection(
    viewModel: FixedCalendarViewModel,
    isProActive: Boolean,
    onOpenPaywall: () -> Unit,
    sparkyViewModel: com.l1khith.calender28.viewmodel.SparkyViewModel? = null
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    var selectedHabitId by remember { mutableStateOf<String?>(null) }
    var showCreateNewHabitScreen by remember { mutableStateOf(false) }

    val selectedHabit = habits.find { it.id == selectedHabitId }

    fun toggleDay(habit: Habit, dayInCycle: Int) {
        val todayEpochDay = HabitCycleEngine.currentEpochDay()
        val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
        val currentCyclePos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)

        if (dayInCycle > currentCyclePos.safeDayInCycle) {
            com.l1khith.calender28.utils.showPlatformToast("Cannot log habit for future days!")
            return
        }

        val isCompleted = habit.completedDays.contains(dayInCycle)
        if (!isCompleted) {
            sparkyViewModel?.triggerHabitComplete(habit.name)
        }
        viewModel.toggleHabitDay(habit.id, currentCyclePos.cycleIndex, dayInCycle, isCompleted)
    }

    if (selectedHabit != null) {
        Dialog(
            onDismissRequest = { selectedHabitId = null },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            HabitDetailScreen(
                habit = selectedHabit,
                onBack = { selectedHabitId = null },
                onToggleDay = { dayIndex ->
                    toggleDay(selectedHabit, dayIndex)
                },
                onUpdateHabit = { updated ->
                    viewModel.updateHabit(updated)
                },
                onDeleteHabit = { habitId ->
                    viewModel.deleteHabit(habitId)
                    selectedHabitId = null
                }
            )
        }
    }

    if (showCreateNewHabitScreen) {
        Dialog(
            onDismissRequest = { showCreateNewHabitScreen = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
        ) {
            NewHabitScreen(
                onDismiss = { showCreateNewHabitScreen = false },
                onCreateHabit = { newHabit ->
                    viewModel.insertHabit(newHabit)
                    showCreateNewHabitScreen = false
                    selectedHabitId = newHabit.id
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Habit Tracker",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "28-Day Habit Cycles",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Build streaks in perfect 4-week blocks",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Button(
                    onClick = {
                        if (isProActive) {
                            showCreateNewHabitScreen = true
                        } else {
                            onOpenPaywall()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.PrimaryContainer),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = MatrixShapes.Md
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = MatrixColors.OnPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Habit", color = MatrixColors.OnPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (habits.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    shape = MatrixShapes.Lg,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Active Habit Cycles",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isProActive) "Tap below to create your first 28-day habit cycle!" else "Upgrade to Pro to track unlimited 28-day habit cycles & streaks.",
                            color = MatrixColors.TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(habits, key = { it.id }) { habit ->
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedHabitId = habit.id },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = habit.name,
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF3B82F6).copy(alpha = 0.15f),
                                        border = BorderStroke(0.5.dp, Color(0xFF3B82F6).copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = habit.category,
                                            color = Color(0xFF60A5FA),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AppIcons.Fire,
                                        contentDescription = "Streak",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${habit.streak} Day Streak • ${habit.progressPercent}% Complete (${habit.completedCount}/28 Days)",
                                        color = MatrixColors.Secondary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "View Habit Detail",
                                tint = MatrixColors.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4x7 Day Grid for Habit (Days 1 to 28)
                        val todayEpochDay = HabitCycleEngine.currentEpochDay()
                        val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
                        val currentPos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)
                        val todayCycleDay = currentPos.safeDayInCycle

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until 4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (col in 1..7) {
                                        val dayNum = row * 7 + col
                                        val isDone = habit.completedDays.contains(dayNum)
                                        val isToday = dayNum == todayCycleDay
                                        val isFuture = dayNum > todayCycleDay

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(30.dp)
                                                .clip(MatrixShapes.Sm)
                                                .background(
                                                    if (isDone) Color(0xFF3B82F6)
                                                    else if (isFuture) MatrixColors.SurfaceContainerHigh.copy(alpha = 0.2f)
                                                    else MatrixColors.SurfaceContainerHigh.copy(alpha = 0.5f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isToday) Color(0xFF60A5FA)
                                                    else if (isDone) Color(0xFF3B82F6)
                                                    else MatrixColors.OutlineVariant.copy(alpha = 0.3f),
                                                    MatrixShapes.Sm
                                                )
                                                .clickable(enabled = !isFuture) { toggleDay(habit, dayNum) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isDone) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Completed",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = "$dayNum",
                                                    color = if (isFuture) MatrixColors.TextSecondary.copy(alpha = 0.3f) else MatrixColors.TextHeader,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Prominent Bottom "Create New Habit" Button Card
        item {
            Card(
                shape = MatrixShapes.Lg,
                colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                border = BorderStroke(1.5.dp, MatrixColors.Primary.copy(alpha = 0.6f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 24.dp)
                    .clickable {
                        if (isProActive) {
                            showCreateNewHabitScreen = true
                        } else {
                            onOpenPaywall()
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New Habit",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create New Habit Cycle",
                        color = MatrixColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHabitScreen(
    onDismiss: () -> Unit,
    onCreateHabit: (Habit) -> Unit
) {
    var habitName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Health") }
    var isReminderEnabled by remember { mutableStateOf(true) }
    var reminderTime by remember { mutableStateOf("07:00 AM") }
    var showTimePicker by remember { mutableStateOf(false) }

    PlatformTimePicker(
        show = showTimePicker,
        initialTime = reminderTime,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { time ->
            reminderTime = time
            showTimePicker = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Habit", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MatrixColors.TextHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixColors.Surface)
            )
        },
        containerColor = MatrixColors.Surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // HABIT NAME
                Column {
                    Text(
                        text = "HABIT NAME",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = habitName,
                        onValueChange = { habitName = it },
                        placeholder = { Text("e.g., Morning Meditation", color = MatrixColors.TextSecondary.copy(alpha = 0.5f)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MatrixColors.TextHeader,
                            unfocusedTextColor = MatrixColors.TextHeader,
                            focusedContainerColor = MatrixColors.SurfaceContainerLow,
                            unfocusedContainerColor = MatrixColors.SurfaceContainerLow,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = MatrixColors.OutlineVariant
                        ),
                        shape = MatrixShapes.Md
                    )
                }

                // CATEGORY
                Column {
                    Text(
                        text = "CATEGORY",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Health", "Work", "Personal").forEach { cat ->
                            val isSelected = cat == selectedCategory
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color(0xFF1E293B) else MatrixColors.SurfaceContainerLow,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFF3B82F6) else MatrixColors.OutlineVariant
                                ),
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    color = if (isSelected) Color(0xFF60A5FA) else MatrixColors.TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // FREQUENCY & CYCLE PREVIEW CARD
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Frequency", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("28-day cycle commitment", color = MatrixColors.TextSecondary, fontSize = 12.sp)
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f))
                            ) {
                                Text("Daily", color = Color(0xFF60A5FA), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "CYCLE PREVIEW",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // 4x7 Clean Outline Day Slots Grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            for (row in 0 until 4) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (col in 1..7) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(28.dp)
                                                .clip(MatrixShapes.Sm)
                                                .background(MatrixColors.SurfaceContainerHigh.copy(alpha = 0.3f))
                                                .border(
                                                    1.dp,
                                                    MatrixColors.OutlineVariant.copy(alpha = 0.5f),
                                                    MatrixShapes.Sm
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                }

                // DAILY REMINDER CARD (USING ICON 9)
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = AppIcons.ReminderTime, contentDescription = null, tint = MatrixColors.TextHeader, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Daily Reminder", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isReminderEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF3B82F6))
                            )
                        }

                        if (isReminderEnabled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Time", color = MatrixColors.TextSecondary, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(reminderTime, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Start 28-Day Cycle Pill Button
            Button(
                onClick = {
                    if (habitName.trim().isNotEmpty()) {
                        val newH = Habit(
                            id = "h_${currentTimeMillis()}",
                            name = habitName.trim(),
                            category = selectedCategory,
                            reminderTime = if (isReminderEnabled) "$reminderTime Daily" else null,
                            isReminderEnabled = isReminderEnabled,
                            completedDays = setOf(),
                            createdAtMs = currentTimeMillis()
                        )
                        onCreateHabit(newH)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (habitName.trim().isNotEmpty()) Color(0xFF3B82F6) else MatrixColors.SurfaceContainerHigh
                )
            ) {
                Text(
                    text = "Start 28-Day Cycle",
                    color = if (habitName.trim().isNotEmpty()) Color.White else MatrixColors.TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habit: Habit,
    onBack: () -> Unit,
    onToggleDay: (Int) -> Unit = {},
    onUpdateHabit: (Habit) -> Unit,
    onDeleteHabit: (String) -> Unit
) {
    val weekHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    // Determine today's day number in the 28-day cycle relative to habit creation
    val todayEpochDay = HabitCycleEngine.currentEpochDay()
    val anchorEpochDay = HabitCycleEngine.getEpochDay(habit.createdAtMs)
    val currentPos = HabitCycleEngine.computePosition(todayEpochDay, anchorEpochDay)
    val todayCycleDay = currentPos.safeDayInCycle

    var showTimePicker by remember { mutableStateOf(false) }

    PlatformTimePicker(
        show = showTimePicker,
        initialTime = habit.reminderTime ?: "07:00 AM",
        onDismiss = { showTimePicker = false },
        onTimeSelected = { newTime ->
            onUpdateHabit(habit.copy(reminderTime = "$newTime Daily"))
            showTimePicker = false
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit.name, color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MatrixColors.TextHeader)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Options", tint = MatrixColors.TextHeader)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MatrixColors.Surface)
            )
        },
        containerColor = MatrixColors.Surface
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CURRENT CYCLE PROGRESS CARD
            item {
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "CURRENT CYCLE PROGRESS",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier.size(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { habit.completedCount.toFloat() / 28f },
                                modifier = Modifier.fillMaxSize(),
                                color = Color(0xFF93C5FD),
                                strokeWidth = 8.dp,
                                trackColor = MatrixColors.OutlineVariant
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${habit.progressPercent}%", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 32.sp)
                                Text("Day $todayCycleDay of 28", color = MatrixColors.TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (habit.completedDays.contains(todayCycleDay))
                                "Today's protocol completed! Keep maintaining solid momentum."
                            else
                                "Today is Day $todayCycleDay. Tap today's box in the 28-day pattern below to log your progress.",
                            color = MatrixColors.TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // STREAK & CONSISTENCY CARDS (SIDE BY SIDE)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MatrixShapes.Lg,
                        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                        border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Fire,
                                    contentDescription = null,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CURRENT STREAK", color = MatrixColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${habit.streak} Days", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = MatrixShapes.Lg,
                        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                        border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CONSISTENCY", color = MatrixColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${habit.consistencyPercent} %", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                    }
                }
            }

            // LONGEST STREAK CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MatrixColors.Secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LONGEST STREAK", color = MatrixColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${habit.longestStreak} Days", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                }
            }

            // CYCLE PATTERN CARD (INTERACTIVE 28-DAY CALENDAR GRID)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Cycle Pattern", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Tap any box in the 28-day pattern below to update progress", color = MatrixColors.TextSecondary, fontSize = 11.sp)
                            }
                            Surface(
                                shape = CircleShape,
                                color = MatrixColors.SurfaceContainerHigh,
                                border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                            ) {
                                Text("Last 28 Days", color = MatrixColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Week Headers Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            weekHeaders.forEach { header ->
                                Text(header, color = MatrixColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4x7 Interactive Dots Grid mapped to 28-day calendar (Day 1 to Day 28)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (row in 0 until 4) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    for (col in 1..7) {
                                        val dayIndex = row * 7 + col
                                        val isToday = dayIndex == todayCycleDay
                                        val isDone = habit.completedDays.contains(dayIndex)
                                        val isFuture = dayIndex > todayCycleDay

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable(enabled = !isFuture) {
                                                    onToggleDay(dayIndex)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isDone) Color(0xFF3B82F6)
                                                        else if (isFuture) MatrixColors.OutlineVariant.copy(alpha = 0.15f)
                                                        else MatrixColors.OutlineVariant.copy(alpha = 0.4f)
                                                    )
                                                    .border(
                                                        width = if (isToday) 2.dp else 0.dp,
                                                        color = if (isToday) Color(0xFF60A5FA) else Color.Transparent,
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isDone) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Completed",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = "$dayIndex",
                                                        color = if (isFuture) MatrixColors.TextSecondary.copy(alpha = 0.35f) else MatrixColors.TextHeader,
                                                        fontSize = 10.sp,
                                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // MANAGEMENT CARD (Reminder Time only)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Column {
                        Text("Management", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.padding(16.dp))

                        HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = AppIcons.ReminderTime, contentDescription = null, tint = MatrixColors.TextSecondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Reminder Time", color = MatrixColors.TextHeader, fontSize = 14.sp)
                                    Text(habit.reminderTime ?: "Off", color = MatrixColors.TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MatrixColors.TextSecondary)
                        }
                    }
                }
            }

            // DELETE PROTOCOL OUTLINE BUTTON WITH Icon (12).svg
            item {
                OutlinedButton(
                    onClick = { onDeleteHabit(habit.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    border = BorderStroke(1.dp, Color(0xFFF87171).copy(alpha = 0.5f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.DeleteProtocol,
                            contentDescription = "Delete",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Protocol", color = Color(0xFFF87171), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

