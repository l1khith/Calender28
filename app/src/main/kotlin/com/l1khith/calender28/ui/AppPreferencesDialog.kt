package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.AppSettingsManager

@Composable
fun AppPreferencesDialog(
    onDismiss: () -> Unit
) {
    val enableSparky by AppSettingsManager.enableSparky.collectAsStateWithLifecycle()
    val enableAnimations by AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()
    val enableSounds by AppSettingsManager.enableSounds.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MatrixShapes.Xl,
                colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MatrixColors.PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AppIcons.Sparky,
                                contentDescription = null,
                                tint = MatrixColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "App Preferences",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.TextHeader
                                )
                            )
                            Text(
                                text = "Customize your companion, effects & sounds",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.5f))

                    // 1. Enable Sparky Switch
                    PreferenceToggleRow(
                        icon = AppIcons.Sparky,
                        title = "Enable Sparky",
                        subtitle = "Show/hide Sparky entirely",
                        checked = enableSparky,
                        onCheckedChange = { AppSettingsManager.setEnableSparky(it) }
                    )

                    HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.3f))

                    // 2. Enable Animations Switch
                    PreferenceToggleRow(
                        icon = Icons.Default.Animation,
                        title = "Enable Animations",
                        subtitle = "UI effects, motion, and animations",
                        checked = enableAnimations,
                        onCheckedChange = { AppSettingsManager.setEnableAnimations(it) }
                    )

                    HorizontalDivider(color = MatrixColors.OutlineVariant.copy(alpha = 0.3f))

                    // 3. Enable Sounds Switch
                    PreferenceToggleRow(
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                        title = "Enable Sounds",
                        subtitle = "Play/hide sound effects",
                        checked = enableSounds,
                        onCheckedChange = { AppSettingsManager.setEnableSounds(it) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatrixShapes.Md,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (checked) MatrixColors.Primary else MatrixColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = title,
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = subtitle,
                    color = MatrixColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MatrixColors.Primary,
                checkedTrackColor = MatrixColors.PrimaryContainer
            )
        )
    }
}
