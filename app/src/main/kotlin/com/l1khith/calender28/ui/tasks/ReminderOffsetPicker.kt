package com.l1khith.calender28.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Multi-alarm reminder selector allowing up to [maxReminders] reminder offsets per task.
 */
@Composable
fun ReminderOffsetPicker(
    selectedOffsets: List<Int>,
    onOffsetsChanged: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    maxReminders: Int = 5
) {
    val presets = remember {
        listOf(
            0 to "At start time",
            10 to "10 min before",
            15 to "15 min before",
            30 to "30 min before",
            60 to "1 hour before",
            1440 to "1 day before"
        )
    }

    var showCustomDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MatrixColors.SurfaceContainerLow)
            .border(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "REMINDERS (${selectedOffsets.size}/$maxReminders)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.Primary
                )
            )

            if (selectedOffsets.size < maxReminders) {
                TextButton(
                    onClick = { showCustomDialog = true },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add custom reminder",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Custom",
                        color = MatrixColors.Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Presets list
        presets.forEach { (offsetMin, label) ->
            val isChecked = selectedOffsets.contains(offsetMin)
            val canCheck = isChecked || selectedOffsets.size < maxReminders

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = canCheck) {
                        val newOffsets = if (isChecked) {
                            selectedOffsets.filter { it != offsetMin }
                        } else {
                            (selectedOffsets + offsetMin).distinct().sorted()
                        }
                        onOffsetsChanged(newOffsets)
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { checked ->
                        val newOffsets = if (checked) {
                            if (selectedOffsets.size < maxReminders) {
                                (selectedOffsets + offsetMin).distinct().sorted()
                            } else selectedOffsets
                        } else {
                            selectedOffsets.filter { it != offsetMin }
                        }
                        onOffsetsChanged(newOffsets)
                    },
                    enabled = canCheck,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MatrixColors.Primary,
                        uncheckedColor = MatrixColors.TextSecondary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (canCheck || isChecked) MatrixColors.TextHeader else MatrixColors.TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp
                    )
                )
            }
        }

        // Custom offsets not matching any preset
        val customOffsets = selectedOffsets.filter { offset -> presets.none { it.first == offset } }
        if (customOffsets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(6.dp))

            customOffsets.forEach { customOffset ->
                val label = formatOffsetLabel(customOffset)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = true,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    onOffsetsChanged(selectedOffsets.filter { it != customOffset })
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MatrixColors.Primary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixColors.TextHeader,
                                fontSize = 13.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = { onOffsetsChanged(selectedOffsets.filter { it != customOffset }) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove reminder",
                            tint = MatrixColors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomOffsetDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { minutes ->
                showCustomDialog = false
                if (minutes in 1..10080 && selectedOffsets.size < maxReminders) {
                    onOffsetsChanged((selectedOffsets + minutes).distinct().sorted())
                }
            }
        )
    }
}

private fun formatOffsetLabel(offsetMin: Int): String {
    return when {
        offsetMin == 0 -> "At start time"
        offsetMin < 60 -> "$offsetMin min before"
        offsetMin % 1440 == 0 -> "${offsetMin / 1440} day${if (offsetMin / 1440 > 1) "s" else ""} before"
        offsetMin % 60 == 0 -> "${offsetMin / 60} hour${if (offsetMin / 60 > 1) "s" else ""} before"
        else -> "${offsetMin / 60}h ${offsetMin % 60}m before"
    }
}

@Composable
private fun CustomOffsetDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableIntStateOf(1) } // 1 = minutes, 60 = hours, 1440 = days

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Custom Reminder",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.TextHeader
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Remind me before event starts:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MatrixColors.TextSecondary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textValue,
                        onValueChange = { textValue = it.filter { ch -> ch.isDigit() }.take(5) },
                        placeholder = { Text("15") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MatrixColors.Primary,
                            unfocusedBorderColor = MatrixColors.OutlineVariant,
                            focusedTextColor = MatrixColors.TextHeader,
                            unfocusedTextColor = MatrixColors.TextHeader
                        )
                    )

                    UnitChip(label = "m", isSelected = selectedUnit == 1) { selectedUnit = 1 }
                    UnitChip(label = "h", isSelected = selectedUnit == 60) { selectedUnit = 60 }
                    UnitChip(label = "d", isSelected = selectedUnit == 1440) { selectedUnit = 1440 }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = textValue.toIntOrNull() ?: 15
                    val totalMinutes = (amount * selectedUnit).coerceIn(1, 10080)
                    onConfirm(totalMinutes)
                }
            ) {
                Text("Add", color = MatrixColors.Primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MatrixColors.TextSecondary)
            }
        },
        containerColor = MatrixColors.SurfaceContainerHigh
    )
}

@Composable
private fun UnitChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MatrixColors.Primary else MatrixColors.SurfaceContainerLow)
            .border(
                1.dp,
                if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) MatrixColors.OnPrimary else MatrixColors.TextHeader,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}
