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
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.PlatformTimePicker


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
        priority: Int
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
    onOpenPaywall: () -> Unit = {}
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
    var reminderTime by remember { mutableStateOf(task?.reminderTime ?: "12:00") }
    var priority by remember { mutableStateOf(task?.priority ?: 1) }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }
    var showTimePicker by remember { mutableStateOf(false) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var customCategoryName by remember { mutableStateOf<String?>(null) }

    val performSave = {
        val finalDesc = if (!customCategoryName.isNullOrEmpty()) {
            "[Category: $customCategoryName] ${description.trim()}"
        } else description.trim()

        Log.d(TAG, "performSave: Saving task title=${title.trim()}, type=$taskTypeLabel, isReminder=$isReminder")

        if (taskTypeLabel == "Recurring") {
            if (!isProActive && existingRecurringCount >= 1 && task == null) {
                Log.d(TAG, "performSave: Paywall triggered for recurring task limit")
                onOpenPaywall()
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
                    null,
                    if (isReminder) reminderTime else null
                )
                onDismiss()
            }
        } else {
            Log.d(TAG, "performSave: Saving normal/scheduled task with id=${task?.id}")
            onSave(task?.id, title.trim(), finalDesc, isReminder, reminderTime, priority)
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

                TextButton(
                    onClick = {
                        if (title.trim().isNotEmpty()) {
                            if (isReminder && !reminderTime.isNullOrEmpty()) {
                                notifPermissionLauncher()
                            } else {
                                performSave()
                            }
                        }
                    },
                    enabled = title.trim().isNotEmpty()
                ) {

                    Text(
                        text = "Save",
                        color = if (title.trim().isNotEmpty()) MatrixColors.Primary else MatrixColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
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
                                            taskTypeLabel = label
                                            isReminder = (label == "Scheduled")
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

                        if (isReminder || taskTypeLabel == "Scheduled" || taskTypeLabel == "Recurring") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showTimePicker = true },
                                shape = MatrixShapes.Md,
                                colors = CardDefaults.cardColors(containerColor = MatrixColors.Surface),
                                border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DateRange,
                                            contentDescription = "Time",
                                            tint = MatrixColors.TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Scheduled Time", color = MatrixColors.TextSecondary, fontSize = 13.sp)
                                    }
                                    Text(reminderTime, color = MatrixColors.Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
        }
    }

    PlatformTimePicker(
        show = showTimePicker,
        initialTime = reminderTime,
        onDismiss = { showTimePicker = false },
        onTimeSelected = { time: String ->
            reminderTime = time
            showTimePicker = false
        }
    )
}


