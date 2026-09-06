package com.l1khith.calender28.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l1khith.calender28.data.RecurrenceType
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.utils.PlatformTimePicker

@Composable
fun RecurringManagerDialog(
    recurringTasks: List<RecurringTask>,
    onDismiss: () -> Unit,
    onSaveRecurringTask: (
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
    ) -> Unit,
    onDeleteRecurringTask: (String) -> Unit,
    onToggleActive: (RecurringTask) -> Unit,
    primaryColor: Color = Color(0xFF2563EB),
    backgroundColor: Color = Color.Black,
    textColor: Color = Color.White,
    textColorSec: Color = Color(0xFF94A3B8)
) {
    var showAddEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<RecurringTask?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = backgroundColor,
            border = BorderStroke(1.dp, Color(0xFF1D4ED8))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recurring Templates",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = textColorSec)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage automated routines that instance into your calendar",
                    color = textColorSec,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Create New Button
                Button(
                    onClick = {
                        taskToEdit = null
                        showAddEditDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Template", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create New Recurring Template", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Templates List
                Box(modifier = Modifier.weight(1f)) {
                    if (recurringTasks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recurring templates found.\nTap button above to create one.",
                                color = textColorSec,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(recurringTasks, key = { it.id }) { template ->
                                RecurringTaskItem(
                                    task = template,
                                    onToggleActive = { onToggleActive(template) },
                                    onEdit = {
                                        taskToEdit = template
                                        showAddEditDialog = true
                                    },
                                    onDelete = { onDeleteRecurringTask(template.id) },
                                    primaryColor = primaryColor,
                                    textColor = textColor,
                                    textColorSec = textColorSec
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditRecurringDialog(
            task = taskToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime ->
                onSaveRecurringTask(id, title, desc, recType, recDays, interval, priority, isActive, endDate, reminderTime)
                showAddEditDialog = false
            },
            primaryColor = primaryColor,
            backgroundColor = backgroundColor,
            textColor = textColor,
            textColorSec = textColorSec
        )
    }
}

@Composable
fun RecurringTaskItem(
    task: RecurringTask,
    onToggleActive: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    primaryColor: Color,
    textColor: Color,
    textColorSec: Color
) {
    val ruleText = when (task.recurrenceType) {
        RecurrenceType.DAILY -> "Every Day"
        RecurrenceType.WEEKDAYS -> "Mon - Fri"
        RecurrenceType.WEEKENDS -> "Sat - Sun"
        RecurrenceType.WEEKLY -> {
            val weekdays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            val daysStr = task.recurrenceDays.sorted().map { weekdays[it] }.joinToString(", ")
            "Weekly on: $daysStr"
        }
        RecurrenceType.MONTHLY -> "1st of Every Month"
        RecurrenceType.YEARLY -> "Jan 1st Every Year"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, if (task.isActive) primaryColor.copy(alpha = 0.5f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = when (task.priority) {
                            3 -> Color(0xFFEF4444)
                            2 -> Color(0xFFF59E0B)
                            else -> Color(0xFF94A3B8)
                        }.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = when (task.priority) { 3 -> "H" 2 -> "M" else -> "L" },
                            color = when (task.priority) {
                                3 -> Color(0xFFEF4444)
                                2 -> Color(0xFFF59E0B)
                                else -> Color(0xFF94A3B8)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = task.title,
                        color = if (task.isActive) textColor else textColorSec,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                val timeStr = if (!task.reminderTime.isNullOrEmpty()) " at ${task.reminderTime}" else ""
                Text(
                    text = "$ruleText$timeStr",
                    color = primaryColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
                if (!task.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        color = textColorSec,
                        fontSize = 12.sp,
                        maxLines = 2
                    )
                }
                if (!task.endDate.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ends: ${task.endDate}",
                        color = Color(0xFFEF4444),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = task.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                )

                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Template",
                        tint = textColorSec,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Template",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRecurringDialog(
    task: RecurringTask?,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        title: String,
        description: String,
        recurrenceType: RecurrenceType,
        recurrenceDays: List<Int>,
        recurrenceInterval: Int,
        priority: Int,
        isActive: Boolean,
        endDate: String,
        reminderTime: String?
    ) -> Unit,
    primaryColor: Color,
    backgroundColor: Color,
    textColor: Color,
    textColorSec: Color
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var recurrenceType by remember { mutableStateOf(task?.recurrenceType ?: RecurrenceType.DAILY) }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(task?.recurrenceDays ?: emptyList()) } }
    var priority by remember { mutableStateOf(task?.priority ?: 1) }
    var isActive by remember { mutableStateOf(task?.isActive ?: true) }
    var endDate by remember { mutableStateOf(task?.endDate ?: "") }
    var isReminder by remember { mutableStateOf(task?.reminderTime != null) }
    var reminderTime by remember { mutableStateOf(task?.reminderTime ?: "12:00") }
    var showTimePicker by remember { mutableStateOf(false) }

    val dividerColor = Color(0xFF1D4ED8)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black,
            border = BorderStroke(1.dp, Color(0xFF1D4ED8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = if (task == null) "Create Recurring Template" else "Edit Recurring Template",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title (Required)", color = textColorSec) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Notes (Optional)", color = textColorSec) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Text("Recurrence Rule", color = textColorSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    var expandedType by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E293B))
                            .clickable { expandedType = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = recurrenceType.name, color = textColor)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Pick Type", tint = primaryColor)
                        }

                        DropdownMenu(
                            expanded = expandedType,
                            onDismissRequest = { expandedType = false },
                            modifier = Modifier.background(Color(0xFF1E293B))
                        ) {
                            for (type in RecurrenceType.values()) {
                                DropdownMenuItem(
                                    text = { Text(text = type.name, color = Color.White) },
                                    onClick = {
                                        recurrenceType = type
                                        expandedType = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (recurrenceType == RecurrenceType.WEEKLY) {
                    item {
                        Text("Select Days of Week", color = textColorSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val daysOfWeekShort = arrayOf("S", "M", "T", "W", "T", "F", "S")
                            for (idx in 0..6) {
                                val isDaySelected = selectedDays.contains(idx)
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (isDaySelected) primaryColor else Color(0xFF1E293B))
                                        .clickable {
                                            if (isDaySelected) {
                                                selectedDays.remove(idx)
                                            } else {
                                                selectedDays.add(idx)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = daysOfWeekShort[idx],
                                        color = if (isDaySelected) Color.White else textColorSec,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Text("Template Priority", color = textColorSec, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val priorities = listOf(
                            Triple(1, "Low", Color(0xFF94A3B8)),
                            Triple(2, "Medium", Color(0xFFF59E0B)),
                            Triple(3, "High", Color(0xFFEF4444))
                        )
                        for ((pVal, label, color) in priorities) {
                            val isSelected = priority == pVal
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .clickable { priority = pVal },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) color else Color.Transparent
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF1E293B)
                                )
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) color else textColorSec,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    OutlinedTextField(
                        value = endDate,
                        onValueChange = { endDate = it },
                        label = { Text("End Date YYYY-MM-DD (Optional)", color = textColorSec) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color(0xFF1E293B)
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminder Alert",
                                tint = if (isReminder) primaryColor else textColorSec,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Time-Critical Reminder", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Trigger exact sound/vibe device alarm", color = textColorSec, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = isReminder,
                            onCheckedChange = { isReminder = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (isReminder) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { showTimePicker = true }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trigger Alarm Time", color = textColor, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(reminderTime, color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Pick Time", tint = primaryColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active State", color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = textColorSec)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.trim().isNotEmpty()) {
                                    val interval = 1
                                    onSave(
                                        task?.id,
                                        title.trim(),
                                        description.trim(),
                                        recurrenceType,
                                        selectedDays.toList(),
                                        interval,
                                        priority,
                                        isActive,
                                        endDate.trim(),
                                        if (isReminder) reminderTime else null
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = title.trim().isNotEmpty()
                        ) {
                            Text("Save Template", color = Color.White)
                        }
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

