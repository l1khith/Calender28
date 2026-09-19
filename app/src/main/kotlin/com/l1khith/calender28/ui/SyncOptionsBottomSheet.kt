package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
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
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.showPlatformToast

@Composable
fun SyncOptionsBottomSheet(
    onDismiss: () -> Unit,
    onSyncSystem: () -> Unit,
    onImportIcs: () -> Unit,
    onExportTasks: () -> Unit,
    isProActive: Boolean,
    onOpenPaywall: () -> Unit
) {
    val isPremium by com.l1khith.calender28.billing.RevenueCatManager.isPremium.collectAsStateWithLifecycle()
    val isPro = isPremium || isProActive
    val cardBg = MatrixColors.SurfaceContainerLow
    val titleColor = MatrixColors.TextHeader
    val subtitleColor = MatrixColors.TextSecondary
    val accentBlue = MatrixColors.Primary

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", color = MatrixColors.TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        title = {
            Column {
                Text(
                    text = "Sync & Transfer Data",
                    color = titleColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select an action to update or backup your matrix tasks and calendar entries:",
                    color = subtitleColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Action 1: Sync from Device Calendar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSyncSystem() },
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(accentBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync",
                                tint = accentBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Sync from Device Calendar",
                                color = titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Import events from system calendar",
                                color = subtitleColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 2: Import from .ics File
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onImportIcs() },
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Import .ics",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Import from .ics File",
                                color = titleColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pick an iCalendar (.ics) file",
                                color = subtitleColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action 3: Export Tasks to File (Pro Gated)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isPro) {
                                onExportTasks()
                            } else {
                                onOpenPaywall()
                            }
                        },
                    shape = MatrixShapes.Lg,
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Export Tasks to File",
                                    color = titleColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (!isPro) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Pro",
                                        tint = MatrixColors.Primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "PRO",
                                        color = MatrixColors.Primary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Save tasks as .csv, .json, or .ics",
                                color = subtitleColor,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        containerColor = MatrixColors.SurfaceContainerLow
    )
}

