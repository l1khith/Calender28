package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.ui.components.UpgradeDialog
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.PlatformTimePicker
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import android.util.Log

private const val TAG = "CreateTaskScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    task: AppTask?,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        title: String,
        description: String?,
        isReminder: Boolean,
        reminderTime: String?,
        priority: Int,
        endDate: String?,
        endTime: String?,
        isAllDay: Boolean,
        reminderOffsetMin: Int?,
        reminderOffsets: List<Int>
    ) -> Unit,
    onSaveRecurring: (
        id: String?,
        title: String,
        description: String?,
        recurrenceType: RecurrenceType,
        recurrenceDays: List<Int>,
        recurrenceInterval: Int,
        priority: Int,
        isActive: Boolean,
        endDate: String?,
        reminderTime: String?
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    existingRecurringCount: Int = 0,
    isProActive: Boolean = false,
    onOpenPaywall: () -> Unit = {},
    onDeleteTask: (AppTask) -> Unit = {},
    onDeleteRecurring: (String) -> Unit = {},
    onStartFocus: ((AppTask) -> Unit)? = null,
    initialDateStr: String? = null,
    initialHour: Int? = null,
    existingTasks: List<AppTask> = emptyList()
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var taskTypeLabel by remember(task) {
        mutableStateOf(
            when {
                task?.isGenerated == 1 || task?.recurringParentId != null -> "Recurring"
                task?.reminder == true -> "Scheduled"
                else -> "Normal"
            }
        )
    }
    var isReminder by remember { mutableStateOf(task?.reminder ?: false) }
    var priority by remember { mutableStateOf(task?.priority ?: 1) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }

    val initialDate = remember(task, initialDateStr) {
        task?.associatedDate ?: initialDateStr ?: com.l1khith.calender28.utils.FixedCalendarHelper.currentFixedDate().toString()
    }
    var startDateStr by remember { mutableStateOf(initialDate) }
    var startTimeStr by remember {
        mutableStateOf(
            task?.reminderTime ?: if (initialHour != null) "%02d:00".format(initialHour) else "09:00"
        )
    }
    var sameDayAsStart by remember {
        mutableStateOf(task?.endDate == null || task.endDate == task.associatedDate)
    }
    var endDateStr by remember {
        mutableStateOf(task?.endDate ?: initialDate)
    }
    var endTimeStr by remember {
        mutableStateOf(
            task?.endTime ?: if (initialHour != null) "%02d:00".format((initialHour + 1) % 24) else "10:00"
        )
    }
    var isAllDay by remember { mutableStateOf(task?.allDay ?: false) }
    var reminderOffsets by remember(task) {
        val initial = task?.effectiveReminderOffsets
        mutableStateOf(if (initial.isNullOrEmpty()) listOf(15) else initial)
    }
    var reminderOffsetMin by remember { mutableStateOf(task?.reminderOffsetMin ?: 15) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val parsedStartDate = remember(startDateStr) {
        com.l1khith.calender28.utils.FixedCalendarHelper.parseDateStr(startDateStr)
            ?: com.l1khith.calender28.utils.FixedCalendarHelper.currentFixedDate()
    }
    val effectiveEndDateStr = if (sameDayAsStart) startDateStr else endDateStr
    val parsedEndDate = remember(effectiveEndDateStr) {
        com.l1khith.calender28.utils.FixedCalendarHelper.parseDateStr(effectiveEndDateStr)
            ?: parsedStartDate
    }

    val startMs = remember(parsedStartDate, startTimeStr, isAllDay) {
        if (isAllDay) com.l1khith.calender28.utils.FixedCalendarHelper.toTimestamp(parsedStartDate, "00:00")
        else com.l1khith.calender28.utils.FixedCalendarHelper.toTimestamp(parsedStartDate, startTimeStr)
    }

    val endMs = remember(parsedEndDate, endTimeStr, isAllDay) {
        if (isAllDay) com.l1khith.calender28.utils.FixedCalendarHelper.toTimestamp(parsedEndDate, "23:59")
        else com.l1khith.calender28.utils.FixedCalendarHelper.toTimestamp(parsedEndDate, endTimeStr)
    }

    val isTimeOrderValid = remember(startMs, endMs, isAllDay) {
        isAllDay || endMs >= startMs
    }

    val durationMinutes = remember(startMs, endMs, isAllDay) {
        if (isAllDay) 1440L
        else if (endMs > startMs) (endMs - startMs) / 60_000L
        else 0L
    }

    val formattedDuration = remember(durationMinutes, isAllDay) {
        when {
            isAllDay -> "All-Day Event"
            durationMinutes >= 60 -> {
                val h = durationMinutes / 60
                val m = durationMinutes % 60
                if (m > 0) "${h}h ${m}m" else "${h}h"
            }
            durationMinutes > 0 -> "${durationMinutes}m"
            else -> "Point-in-time"
        }
    }

    // Live Conflict Detection against existingTasks
    val detectedConflict = remember(startMs, endMs, isAllDay, existingTasks, task) {
        if (isAllDay || startMs >= endMs) null
        else {
            existingTasks.firstOrNull { other ->
                other.id != task?.id && !other.allDay && other.utcTimestamp != null && run {
                    val otherStart = other.utcTimestamp!!
                    val otherEnd = other.endUtcTimestamp ?: (otherStart + 60 * 60_000L)
                    startMs < otherEnd && otherStart < endMs
                }
            }
        }
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var customCategoryName by remember { mutableStateOf<String?>(null) }
    val isPremium by com.l1khith.calender28.billing.RevenueCatManager.isPremium.collectAsStateWithLifecycle()
    val isPro = isPremium || isProActive
    var showUpgradeDialog by remember { mutableStateOf(false) }

    val performSave = {
        val finalDesc = if (!customCategoryName.isNullOrEmpty()) {
            "[Category: $customCategoryName] ${description.trim()}"
        } else description.trim()

        Log.d(TAG, "performSave: Saving task title=${title.trim()}, type=$taskTypeLabel, isReminder=$isReminder, isAllDay=$isAllDay")

        if (taskTypeLabel == "Recurring") {
            if (!isPro && existingRecurringCount >= 5 && task == null) {
                Log.d(TAG, "performSave: UpgradeDialog triggered for recurring task limit")
                showUpgradeDialog = true
            } else {
                val recurringIdToSave = task?.recurringParentId ?: task?.id
                Log.d(TAG, "performSave: Saving recurring task with id=$recurringIdToSave")
                onSaveRecurring(
                    recurringIdToSave,
                    title.trim(),
                    finalDesc,
                    recurrenceType,
                    emptyList(),
                    1,
                    priority,
                    true,
                    if (sameDayAsStart) null else effectiveEndDateStr,
                    if (isReminder && !isAllDay) startTimeStr else null
                )
                onDismiss()
            }
        } else {
            Log.d(TAG, "performSave: Saving normal/scheduled task with id=${task?.id}")
            onSave(
                task?.id,
                title.trim(),
                finalDesc,
                isReminder || taskTypeLabel == "Scheduled",
                if (isAllDay) null else startTimeStr,
                priority,
                if (sameDayAsStart) null else effectiveEndDateStr,
                if (isAllDay) null else endTimeStr,
                isAllDay,
                reminderOffsets.firstOrNull(),
                reminderOffsets
            )
            onDismiss()
        }
    }

    val notifPermissionLauncher = com.l1khith.calender28.utils.rememberNotificationPermissionLauncher(
        onGranted = { performSave() },
        onDenied = { performSave() }
    )


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatrixColors.Surface)
            .statusBarsPadding()
    ) {

        // Top App Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MatrixColors.Surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MatrixColors.TextHeader
                    )
                }

                Text(
                    text = if (task == null) "Create Task" else "Edit Task",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (task != null) {
                        IconButton(
                            onClick = {
                                val parentId = task.recurringParentId ?: if (task.id.startsWith("gen_")) {
                                    val parts = task.id.split("_")
                                    if (parts.size >= 3) parts[1] else task.id
                                } else null

                                if (parentId != null || taskTypeLabel == "Recurring") {
                                    onDeleteRecurring(parentId ?: task.id)
                                } else {
                                    onDeleteTask(task)
                                }
                                onDismiss()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Task",
                                tint = Color(0xFFEF4444)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    TextButton(
                        onClick = {
                            if (title.trim().isNotEmpty() && isTimeOrderValid) {
                                if (isReminder && !isAllDay && !startTimeStr.isNullOrEmpty()) {
                                    notifPermissionLauncher()
                                } else {
                                    performSave()
                                }
                            }
                        },
                        enabled = title.trim().isNotEmpty() && isTimeOrderValid
                    ) {
                        Text(
                            text = "Save",
                            color = if (title.trim().isNotEmpty() && isTimeOrderValid) MatrixColors.Primary else MatrixColors.TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
            HorizontalDivider(color = MatrixColors.OutlineVariant, thickness = 1.dp)
        }


        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Name Section
            item {
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Task Name",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MatrixShapes.Sm)
                                .background(MatrixColors.Surface)
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = {
                                        Text(
                                            text = "e.g. Finalize Q3 Report",
                                            color = MatrixColors.TextSecondary.copy(alpha = 0.5f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MatrixColors.Surface,
                                        unfocusedContainerColor = MatrixColors.Surface,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = MatrixColors.TextHeader,
                                        unfocusedTextColor = MatrixColors.TextHeader
                                    )
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(if (title.isNotEmpty()) MatrixColors.Primary else MatrixColors.OutlineVariant)
                                )
                            }
                        }
                    }
                }
            }

            // Task Type Section
            item {
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Task Type",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MatrixShapes.Md)
                                .background(MatrixColors.Surface)
                                .border(1.dp, MatrixColors.OutlineVariant, MatrixShapes.Md)
                                .padding(4.dp)
                        ) {
                            val types = listOf(
                                Pair("Normal", taskTypeLabel == "Normal"),
                                Pair("Scheduled", taskTypeLabel == "Scheduled"),
                                Pair("Recurring", taskTypeLabel == "Recurring")
                            )
                            for ((label, isSelected) in types) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MatrixShapes.Sm)
                                        .background(if (isSelected) MatrixColors.PrimaryContainer else Color.Transparent)
                                        .clickable {
                                            if (label == "Recurring" && !isPro && existingRecurringCount >= 5 && task == null) {
                                                showUpgradeDialog = true
                                            } else {
                                                taskTypeLabel = label
                                                if (label == "Scheduled" || label == "Recurring") {
                                                    isReminder = true
                                                } else if (label == "Normal") {
                                                    isReminder = false
                                                }
                                            }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) MatrixColors.OnPrimaryContainer else MatrixColors.TextSecondary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        if (taskTypeLabel == "Recurring") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Recurrence Pattern",
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val recOptions = listOf(
                                    RecurrenceType.DAILY to "Daily",
                                    RecurrenceType.WEEKDAYS to "Weekdays",
                                    RecurrenceType.WEEKLY to "Weekly",
                                    RecurrenceType.MONTHLY to "Monthly"
                                )
                                for ((rType, rLabel) in recOptions) {
                                    val isSelected = recurrenceType == rType
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { recurrenceType = rType },
                                        shape = MatrixShapes.Sm,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MatrixColors.PrimaryContainer else Color.Transparent
                                        ),
                                        border = BorderStroke(1.dp, if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp).fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = rLabel,
                                                color = if (isSelected) MatrixColors.OnPrimaryContainer else MatrixColors.TextHeader,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // All-Day Switch
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MatrixShapes.Sm)
                                .background(MatrixColors.Surface)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "All-Day Event",
                                color = MatrixColors.TextHeader,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Switch(
                                checked = isAllDay,
                                onCheckedChange = { isAllDay = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MatrixColors.OnPrimary,
                                    checkedTrackColor = MatrixColors.Primary
                                )
                            )
                        }

                        if (!isAllDay) {
                            Spacer(modifier = Modifier.height(12.dp))

                            // ── START TIME & DATE ──
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MatrixShapes.Md,
                                colors = CardDefaults.cardColors(containerColor = MatrixColors.Surface),
                                border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "START",
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { showStartDatePicker = true }) {
                                            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MatrixColors.Primary)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(startDateStr, color = MatrixColors.TextHeader, fontWeight = FontWeight.SemiBold)
                                        }

                                        TextButton(onClick = { showStartTimePicker = true }) {
                                            Text(startTimeStr, color = MatrixColors.Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // ── END TIME & DATE ──
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MatrixShapes.Md,
                                colors = CardDefaults.cardColors(containerColor = MatrixColors.Surface),
                                border = BorderStroke(1.dp, if (!isTimeOrderValid) MatrixColors.Error else MatrixColors.OutlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "END",
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.clickable {
                                                sameDayAsStart = !sameDayAsStart
                                                if (sameDayAsStart) endDateStr = startDateStr
                                            }
                                        ) {
                                            Checkbox(
                                                checked = sameDayAsStart,
                                                onCheckedChange = {
                                                    sameDayAsStart = it
                                                    if (it) endDateStr = startDateStr
                                                },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Same day as start",
                                                color = MatrixColors.TextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (!sameDayAsStart) {
                                            TextButton(onClick = { showEndDatePicker = true }) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MatrixColors.Secondary)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(endDateStr, color = MatrixColors.TextHeader, fontWeight = FontWeight.SemiBold)
                                            }
                                        } else {
                                            Text(
                                                text = "(Ends on $startDateStr)",
                                                color = MatrixColors.TextSecondary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                        }

                                        TextButton(onClick = { showEndTimePicker = true }) {
                                            Text(endTimeStr, color = MatrixColors.Secondary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        }
                                    }
                                }
                            }

                            // Duration and Validation Error
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MatrixColors.Primary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Duration: $formattedDuration",
                                        color = MatrixColors.Primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (!isTimeOrderValid) {
                                    Text(
                                        text = "⚠️ End time must be after start",
                                        color = MatrixColors.Error,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // ── MULTI-ALARM REMINDERS ──
                            Spacer(modifier = Modifier.height(10.dp))
                            com.l1khith.calender28.ui.tasks.ReminderOffsetPicker(
                                selectedOffsets = reminderOffsets,
                                onOffsetsChanged = {
                                    reminderOffsets = it
                                    reminderOffsetMin = it.firstOrNull() ?: 15
                                }
                            )
                        }

                        // ── LIVE CONFLICT DETECTION CARD ──
                        if (detectedConflict != null && !isAllDay) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = MatrixShapes.Md,
                                colors = CardDefaults.cardColors(containerColor = MatrixColors.Error.copy(alpha = 0.12f)),
                                border = BorderStroke(1.dp, MatrixColors.Error.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚠️ Conflict detected",
                                        color = MatrixColors.Error,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Overlaps with '${detectedConflict.title}' (${detectedConflict.formattedTimeRange})",
                                        color = MatrixColors.TextHeader,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                val freeSlot = com.l1khith.calender28.utils.ConflictResolver.findConflictFreeSlot(
                                                    candidateDate = startDateStr,
                                                    durationMinutes = if (durationMinutes > 0) durationMinutes else 60L,
                                                    existingTasks = existingTasks,
                                                    ignoreTaskId = task?.id
                                                )
                                                if (freeSlot != null) {
                                                    startTimeStr = freeSlot
                                                    val durMs = if (durationMinutes > 0) durationMinutes * 60_000L else 60 * 60_000L
                                                    val newEndMs = com.l1khith.calender28.utils.FixedCalendarHelper.toTimestamp(parsedStartDate, freeSlot) + durMs
                                                    val endMinTotal = (newEndMs / 60_000L) % 1440L
                                                    endTimeStr = "%02d:%02d".format((endMinTotal / 60).toInt(), (endMinTotal % 60).toInt())
                                                }
                                            },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Auto-Adjust Time", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            text = "Save Anyway",
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category Section
            item {
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        val defaultCategories = listOf(
                            Pair("Personal", MatrixColors.Tertiary),
                            Pair("Work", MatrixColors.Secondary),
                            Pair("Health", MatrixColors.Error),
                            Pair("Finance", Color(0xFF10B981)),
                            Pair("Social", Color(0xFF8B5CF6)),
                            Pair("Education", Color(0xFFF59E0B))
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for ((label, color) in defaultCategories) {
                                val isSelected = label.equals(customCategoryName, ignoreCase = true)
                                Card(
                                    modifier = Modifier.clickable {
                                        customCategoryName = if (isSelected) null else label
                                    },
                                    shape = MatrixShapes.Xl,
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
                                    ),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) color else MatrixColors.OutlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = if (isSelected) color else MatrixColors.TextHeader,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Notes Section
            item {
                Card(
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Notes",
                                tint = MatrixColors.TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Notes (Optional)",
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Add additional details...", color = MatrixColors.TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 90.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MatrixColors.Surface,
                                unfocusedContainerColor = MatrixColors.Surface,
                                focusedBorderColor = MatrixColors.Primary,
                                unfocusedBorderColor = MatrixColors.OutlineVariant,
                                focusedTextColor = MatrixColors.TextHeader,
                                unfocusedTextColor = MatrixColors.TextHeader
                            )
                        )
                    }
                }
            }

            // Focus Mode Quick Access (For existing incomplete tasks)
            if (task != null && !task.completed && onStartFocus != null) {
                item {
                    Card(
                        shape = MatrixShapes.Lg,
                        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                        border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = AppIcons.Stopwatch,
                                    contentDescription = null,
                                    tint = MatrixColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Distraction-Free Focus",
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Run full-screen countdown timer or stopwatch for this task",
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (task.hasEverFocused) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = MatrixShapes.Sm,
                                    color = MatrixColors.Primary.copy(alpha = 0.12f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Previously focused: ${task.formattedFocusDuration} (${task.focusCount} sessions)",
                                        color = MatrixColors.Primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    onStartFocus(task)
                                    onDismiss()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = MatrixShapes.Md,
                                colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Stopwatch,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "START FOCUS SESSION",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showStartDatePicker) {
        FixedDatePickerDialog(
            initialDateStr = startDateStr,
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { selectedDate ->
                startDateStr = selectedDate
                if (sameDayAsStart) {
                    endDateStr = selectedDate
                }
                showStartDatePicker = false
            }
        )
    }

    if (showEndDatePicker) {
        FixedDatePickerDialog(
            initialDateStr = endDateStr,
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { selectedDate ->
                endDateStr = selectedDate
                showEndDatePicker = false
            }
        )
    }

    PlatformTimePicker(
        show = showStartTimePicker,
        initialTime = startTimeStr,
        onDismiss = { showStartTimePicker = false },
        onTimeSelected = { time: String ->
            startTimeStr = time
            showStartTimePicker = false
        }
    )

    PlatformTimePicker(
        show = showEndTimePicker,
        initialTime = endTimeStr,
        onDismiss = { showEndTimePicker = false },
        onTimeSelected = { time: String ->
            endTimeStr = time
            showEndTimePicker = false
        }
    )

    if (showUpgradeDialog) {
        UpgradeDialog(
            title = "Recurring Task Limit Reached",
            message = "Free users can track up to 5 active recurring tasks. Upgrade to Pro for unlimited recurring tasks.",
            onDismiss = { showUpgradeDialog = false },
            onUpgrade = {
                showUpgradeDialog = false
                onOpenPaywall()
            }
        )
    }
}


