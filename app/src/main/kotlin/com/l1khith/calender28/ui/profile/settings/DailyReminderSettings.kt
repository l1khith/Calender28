package com.l1khith.calender28.ui.profile.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.service.NotificationPermissionHelper
import com.l1khith.calender28.ui.TimePickerDialog
import com.l1khith.calender28.ui.theme.MatrixColors
import java.util.Locale

@Composable
fun DailyReminderSettings(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showTimePicker by remember { mutableStateOf(false) }
    val hasPermission = remember { NotificationPermissionHelper.hasPermission(context) }

    val formattedTime = remember(hour, minute) {
        val ampm = if (hour >= 12) "PM" else "AM"
        val h12 = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        String.format(Locale.US, "%02d:%02d %s", h12, minute, ampm)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Daily Morning Briefing",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader
            )
        )
        Text(
            text = "Receive a personalized overview of your tasks every morning.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixColors.TextSecondary
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MatrixColors.SurfaceContainerHigh,
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notification",
                            tint = if (enabled) MatrixColors.Primary else MatrixColors.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Morning Briefing",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MatrixColors.TextHeader
                                )
                            )
                            Text(
                                text = if (enabled) "Scheduled at $formattedTime" else "Disabled",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MatrixColors.Primary,
                            checkedTrackColor = MatrixColors.Primary.copy(alpha = 0.3f),
                            uncheckedThumbColor = MatrixColors.TextSecondary,
                            uncheckedTrackColor = MatrixColors.SurfaceContainerHighest
                        )
                    )
                }

                if (enabled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(
                        color = MatrixColors.OutlineVariant.copy(alpha = 0.5f),
                        thickness = 1.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showTimePicker = true }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Reminder Time",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MatrixColors.TextHeader
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MatrixColors.Primary.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.Primary
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                if (!hasPermission && enabled) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Permission warning",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Notification permission is required for morning briefing to appear.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        val initialString = String.format(Locale.US, "%02d:%02d", hour, minute)
        TimePickerDialog(
            initialTime = initialString,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { timeStr ->
                val parts = timeStr.split(":")
                val newHour = parts.getOrNull(0)?.toIntOrNull() ?: hour
                var newMinute = parts.getOrNull(1)?.toIntOrNull() ?: minute
                // Snap to 15-minute granularity
                newMinute = ((newMinute + 7) / 15 * 15) % 60
                onTimeChange(newHour, newMinute)
                showTimePicker = false
            },
            primaryColor = MatrixColors.Primary,
            backgroundColor = MatrixColors.Surface
        )
    }
}
