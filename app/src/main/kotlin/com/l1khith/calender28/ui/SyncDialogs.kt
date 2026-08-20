package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
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
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.FixedCalendarHelper

@Composable
fun SyncDialog(
    onDismiss: () -> Unit,
    onSyncSystem: () -> Unit,
    onImportIcs: () -> Unit
) {
    val cardBg = Color(0xFF1E293B)
    val titleColor = Color.White
    val subtitleColor = Color(0xFF94A3B8)
    val accentBlue = Color(0xFF3B82F6)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.Black,
            border = BorderStroke(1.dp, Color(0xFF1D4ED8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Calendar Sync & Import",
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Card 1: System Sync
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSyncSystem() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D4ED8).copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = accentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Sync Google Calendar / Outlook",
                                color = titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Imports events from Android system calendars",
                                color = subtitleColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Card 2: Import ICS File / Text
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onImportIcs() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(accentBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import ICS",
                                tint = accentBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "Import iCalendar (.ics File / Text)",
                                color = titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pick .ics file from device or paste text",
                                color = subtitleColor,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close", color = accentBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun IcsImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var icsText by remember { mutableStateOf("") }
    val primaryAccent = Color(0xFF3B82F6)

    val pickFileLauncher = rememberFilePickerLauncher { fileContent ->
        icsText = fileContent
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black,
            border = BorderStroke(1.dp, Color(0xFF1D4ED8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Import iCalendar (.ics)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Paste or Pick .ics Content:",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { pickFileLauncher() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, primaryAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = "📁 Pick File",
                            color = primaryAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = icsText,
                    onValueChange = { icsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    label = { Text("BEGIN:VCALENDAR ... END:VCALENDAR", color = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = primaryAccent,
                        unfocusedBorderColor = Color(0xFF1E293B)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (icsText.isNotBlank()) {
                                onImport(icsText)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryAccent),
                        enabled = icsText.isNotBlank()
                    ) {
                        Text("Import & Convert", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ExportTasksDialog(
    monthName: String = FixedCalendarHelper.getMonthName(FixedCalendarHelper.currentFixedDate().month),
    year: Int = FixedCalendarHelper.currentFixedDate().year,
    onDismiss: () -> Unit,
    onExportFormat: (format: String, currentMonthOnly: Boolean) -> Unit
) {
    var selectedFormat by remember { mutableStateOf("csv") }
    var currentMonthOnly by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📁 Export Matrix Tasks", color = MatrixColors.TextHeader, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text("Export tasks to your device's Downloads folder.", color = MatrixColors.TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(14.dp))

                // Month filter option
                Surface(
                    shape = MatrixShapes.Md,
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { currentMonthOnly = !currentMonthOnly }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Month Only",
                                color = MatrixColors.TextHeader,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "$monthName $year",
                                color = MatrixColors.Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Checkbox(
                            checked = currentMonthOnly,
                            onCheckedChange = { currentMonthOnly = it },
                            colors = CheckboxDefaults.colors(checkedColor = MatrixColors.Primary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text("Select file format:", color = MatrixColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))

                listOf("csv" to "Spreadsheet (.csv)", "ics" to "iCalendar (.ics)", "json" to "Data Object (.json)").forEach { (format, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFormat = format }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFormat == format,
                            onClick = { selectedFormat = format },
                            colors = RadioButtonDefaults.colors(selectedColor = MatrixColors.Primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, color = MatrixColors.TextHeader, fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = MatrixShapes.Sm,
                    color = MatrixColors.SurfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "💾 Saved to: Downloads/Calender28/",
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onExportFormat(selectedFormat, currentMonthOnly)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
            ) {
                Text("Export to Downloads", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MatrixColors.TextSecondary)
            }
        },
        containerColor = MatrixColors.SurfaceContainerLow
    )
}

