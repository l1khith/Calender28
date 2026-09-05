package com.l1khith.calender28.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.viewmodel.FocusViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.service.FocusSessionManager
import com.l1khith.calender28.service.FocusState
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val MOTIVATIONAL_QUOTES = listOf(
    "You've got this! Keep going!",
    "Stay in the zone. Every minute counts.",
    "Focus is a superpower. Build your momentum!",
    "One task at a time. Pure clarity.",
    "Deep work yields extraordinary results.",
    "Discipline is choosing between what you want now and what you want most.",
    "Small steps in deep focus create monumental success.",
    "Eliminate distractions. Protect your attention."
)

// ═══════════════════════════════════════════════════════════════════════════════
// 1. FOCUS SETUP DIALOG
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FocusSetupDialog(
    task: AppTask,
    onDismiss: () -> Unit,
    onStartFocus: (mode: String, durationMinutes: Int, pinScreen: Boolean) -> Unit
) {
    var selectedMode by remember { mutableStateOf("timer") } // "timer" or "stopwatch"
    var durationMinutes by remember { mutableStateOf(25) }
    var lockScreenWithPinning by remember { mutableStateOf(true) }

    val presetDurations = listOf(15, 25, 30, 45, 60)

    val estimatedCompletionTime = remember(durationMinutes) {
        val targetMs = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(targetMs))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = MatrixShapes.Xl,
            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer),
            border = BorderStroke(1.5.dp, MatrixColors.Primary.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon & Title
                Surface(
                    shape = CircleShape,
                    color = MatrixColors.Primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.4f)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎯", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "FOCUS MODE",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Task Summary Pill
                Surface(
                    shape = MatrixShapes.Md,
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝 ${task.title}",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1
                        )
                        if (!task.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.description,
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mode Tabs (Timer vs Stopwatch)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MatrixShapes.Lg)
                        .background(MatrixColors.SurfaceContainerLow)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Timer Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = "timer" },
                        shape = MatrixShapes.Md,
                        color = if (selectedMode == "timer") MatrixColors.Primary else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = AppIcons.Stopwatch,
                                contentDescription = null,
                                tint = if (selectedMode == "timer") Color.Black else MatrixColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TIMER",
                                color = if (selectedMode == "timer") Color.Black else MatrixColors.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Stopwatch Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedMode = "stopwatch" },
                        shape = MatrixShapes.Md,
                        color = if (selectedMode == "stopwatch") MatrixColors.Primary else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = AppIcons.Stopwatch,
                                contentDescription = null,
                                tint = if (selectedMode == "stopwatch") Color.Black else MatrixColors.TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STOPWATCH",
                                color = if (selectedMode == "stopwatch") Color.Black else MatrixColors.TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedMode == "timer") {
                    // Duration Picker
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DURATION",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Stepper (- / +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { durationMinutes = (durationMinutes - 5).coerceAtLeast(5) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MatrixColors.SurfaceContainerHigh)
                            ) {
                                Text("−", color = MatrixColors.TextHeader, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.width(18.dp))

                            Text(
                                text = "$durationMinutes min",
                                color = MatrixColors.TextHeader,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black
                            )

                            Spacer(modifier = Modifier.width(18.dp))

                            IconButton(
                                onClick = { durationMinutes = (durationMinutes + 5).coerceAtMost(180) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MatrixColors.SurfaceContainerHigh)
                            ) {
                                Text("+", color = MatrixColors.TextHeader, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Preset Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (preset in presetDurations) {
                                val isSelected = durationMinutes == preset
                                Surface(
                                    shape = MatrixShapes.Sm,
                                    color = if (isSelected) MatrixColors.Primary.copy(alpha = 0.2f) else MatrixColors.SurfaceContainerHigh,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.clickable { durationMinutes = preset }
                                ) {
                                    Text(
                                        text = "${preset}m",
                                        color = if (isSelected) MatrixColors.Primary else MatrixColors.TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AppIcons.Stopwatch,
                                contentDescription = null,
                                tint = MatrixColors.Secondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Estimated finish: $estimatedCompletionTime",
                                color = MatrixColors.Secondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Stopwatch Info
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Open-Ended Session",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Timer counts up from 00:00 until you tap Stop. Best for deep reading, studying, or open-ended tasks.",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Screen Pinning Toggle
                Surface(
                    shape = MatrixShapes.Md,
                    color = if (lockScreenWithPinning) MatrixColors.Primary.copy(alpha = 0.12f) else MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(
                        1.dp,
                        if (lockScreenWithPinning) MatrixColors.Primary.copy(alpha = 0.5f) else MatrixColors.OutlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { lockScreenWithPinning = !lockScreenWithPinning }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔒", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Deep Focus Lock (Screen Pinning)",
                                    color = MatrixColors.TextHeader,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (lockScreenWithPinning) "Pins app to screen • Exiting forfeits +5 CalCoins" else "Standard mode • No screen pinning",
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Switch(
                            checked = lockScreenWithPinning,
                            onCheckedChange = { lockScreenWithPinning = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MatrixColors.Primary,
                                checkedTrackColor = MatrixColors.PrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Start Button
                Button(
                    onClick = {
                        onStartFocus(selectedMode, durationMinutes, lockScreenWithPinning)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MatrixShapes.Lg,
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                ) {
                    Text(
                        text = "▶️ START FOCUS SESSION",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = MatrixColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. ACTIVE FOCUS OVERLAY (PURE BLACK DISTRACTION-FREE)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FocusActiveOverlay(
    activeState: FocusState.Active,
    onTogglePause: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit
) {
    // Keep Screen ON during active focus
    val currentView = LocalView.current
    val context = LocalContext.current
    val activity = context as? Activity
    var showBreakFocusDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }

    // Screen Pinning lifecycle: pin screen upon entering active focus, unpin when leaving
    DisposableEffect(activeState.isScreenPinned) {
        if (activeState.isScreenPinned && activity != null) {
            com.l1khith.calender28.utils.ScreenPinningHelper.startPinning(activity)
        }
        onDispose {
            if (activeState.isScreenPinned && activity != null) {
                com.l1khith.calender28.utils.ScreenPinningHelper.stopPinning(activity)
            }
        }
    }

    // Detect if user unpinned during active session
    LaunchedEffect(activeState.elapsedSeconds) {
        if (activeState.isScreenPinned && activeState.elapsedSeconds > 3) {
            if (!com.l1khith.calender28.utils.ScreenPinningHelper.isPinned(context)) {
                // Focus broken by user unpinning the app!
                onCancel()
            }
        }
    }

    var quoteIndex by remember { mutableStateOf(0) }
    LaunchedEffect(activeState.elapsedSeconds) {
        if (activeState.elapsedSeconds > 0 && activeState.elapsedSeconds % 300 == 0) {
            quoteIndex = (quoteIndex + 1) % MOTIVATIONAL_QUOTES.size
        }
    }

    val currentQuote = MOTIVATIONAL_QUOTES[quoteIndex]

    // Pulsing animation when paused
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pauseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pauseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(24.dp)
    ) {
        // Top Bar: Dismiss / Exit Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF1E1E1E),
                modifier = Modifier
                    .size(38.dp)
                    .clickable { showBreakFocusDialog = true }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit Focus",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeState.isScreenPinned) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF3B82F6).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF3B82F6))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("🔒", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PINNED",
                                color = Color(0xFF60A5FA),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = if (activeState.isPaused) Color(0xFFEAB308).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, if (activeState.isPaused) Color(0xFFEAB308) else Color(0xFF10B981))
                ) {
                    Text(
                        text = if (activeState.isPaused) "PAUSED" else "LIVE",
                        color = if (activeState.isPaused) Color(0xFFEAB308) else Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Center: Huge Timer & Task Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task Name
            Text(
                text = activeState.task.title,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Mode subtitle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = AppIcons.Stopwatch,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (activeState.mode == "timer") {
                        "Timer • ${activeState.targetDurationSeconds / 60} min"
                    } else {
                        "Stopwatch Mode"
                    },
                    color = Color(0xFF94A3B8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Big Digital Timer Display
            Text(
                text = activeState.formattedTime,
                color = if (activeState.isPaused) Color.White.copy(alpha = pauseAlpha) else Color.White,
                fontSize = 68.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )

            // Progress bar for Timer mode
            if (activeState.mode == "timer") {
                Spacer(modifier = Modifier.height(18.dp))
                LinearProgressIndicator(
                    progress = { activeState.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = Color(0xFF3B82F6),
                    trackColor = Color(0xFF1E293B),
                )
            }

            if (activeState.isScreenPinned) {
                Spacer(modifier = Modifier.height(18.dp))
                Surface(
                    shape = MatrixShapes.Md,
                    color = Color(0xFF1E293B).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth(0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔒", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Screen Pinned • Exiting forfeits +5 CalCoins",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Controls: Pause/Resume & Stop/Finish
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume Button
                Button(
                    onClick = onTogglePause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeState.isPaused) Color(0xFF10B981) else Color(0xFF334155)
                    ),
                    shape = MatrixShapes.Lg,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = if (activeState.isPaused) "▶️ RESUME" else "⏸️ PAUSE",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Stop / Finish Button
                Button(
                    onClick = {
                        if (activity != null) {
                            com.l1khith.calender28.utils.ScreenPinningHelper.stopPinning(activity)
                        }
                        onStop()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = MatrixShapes.Lg,
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "⏹️ FINISH",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Bottom: Rotating Motivation Quote
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MatrixShapes.Md,
                color = Color(0xFF111827),
                border = BorderStroke(1.dp, Color(0xFF1F2937)),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("💡", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "\"$currentQuote\"",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (showBreakFocusDialog) {
        AlertDialog(
            onDismissRequest = { showBreakFocusDialog = false },
            icon = { Text("⚠️", fontSize = 28.sp) },
            title = {
                Text(
                    text = "Break Focus Session?",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (activeState.isScreenPinned) {
                        "Screen pinning will be stopped. Exiting will cancel this session and forfeit your +5 CalCoins reward."
                    } else {
                        "Are you sure you want to stop early? No CalCoins will be awarded."
                    },
                    color = MatrixColors.TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBreakFocusDialog = false
                        if (activity != null) {
                            com.l1khith.calender28.utils.ScreenPinningHelper.stopPinning(activity)
                        }
                        onCancel()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = MatrixShapes.Md
                ) {
                    Text("Break Focus", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBreakFocusDialog = false }) {
                    Text("Stay Focused", color = MatrixColors.Primary)
                }
            },
            containerColor = MatrixColors.SurfaceContainer,
            shape = MatrixShapes.Lg
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 3. FOCUS COMPLETION OVERLAY (CELEBRATION)
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FocusCompletionOverlay(
    completedState: FocusState.Completed,
    onDone: (markTaskDone: Boolean) -> Unit,
    onAgain: () -> Unit
) {
    // Gentle celebration scale animation
    val scaleAnim = remember { Animatable(0.8f) }
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000).copy(alpha = 0.95f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .scale(scaleAnim.value),
            shape = MatrixShapes.Xl,
            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer),
            border = BorderStroke(2.dp, Color(0xFF10B981).copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Celebration Emojis
                Text("🎉 ✨ 🏆", fontSize = 36.sp)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CONGRATULATIONS!",
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "You completed your focus session!",
                    color = MatrixColors.TextSecondary,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Task & Stats Card
                Surface(
                    shape = MatrixShapes.Lg,
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📝 ${completedState.task.title}",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MatrixShapes.Sm,
                                color = MatrixColors.Primary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = AppIcons.Stopwatch,
                                        contentDescription = null,
                                        tint = MatrixColors.Primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (completedState.mode == "timer") "Timer Mode" else "Stopwatch",
                                        color = MatrixColors.Primary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AppIcons.Stopwatch,
                                    contentDescription = null,
                                    tint = MatrixColors.Secondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = completedState.formattedDuration,
                                    color = MatrixColors.Secondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // DONE Button (Completes task)
                Button(
                    onClick = { onDone(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = MatrixShapes.Lg,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text(
                        text = "✅ MARK TASK COMPLETE",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AGAIN Button
                OutlinedButton(
                    onClick = onAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = MatrixShapes.Lg,
                    border = BorderStroke(1.dp, MatrixColors.Primary)
                ) {
                    Text(
                        text = "🔄 FOCUS AGAIN",
                        color = MatrixColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { onDone(false) }) {
                    Text(
                        text = "Keep Task Incomplete & Close",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 4. FOCUS STATS & ANALYTICS DIALOG
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun FocusStatsDialog(
    focusViewModel: FocusViewModel,
    onDismiss: () -> Unit
) {
    val sessions by focusViewModel.allSessions.collectAsStateWithLifecycle()
    val totalFocusSeconds by focusViewModel.totalFocusSeconds.collectAsStateWithLifecycle()
    val completedCount by focusViewModel.completedSessionCount.collectAsStateWithLifecycle()

    FocusStatsDialog(
        sessions = sessions,
        totalFocusSeconds = totalFocusSeconds,
        completedCount = completedCount,
        onDismiss = onDismiss
    )
}

@Composable
fun FocusStatsDialog(
    sessions: List<FocusSession>,
    totalFocusSeconds: Long,
    completedCount: Int,
    onDismiss: () -> Unit
) {
    val totalHours = totalFocusSeconds / 3600
    val totalMins = (totalFocusSeconds % 3600) / 60

    val totalTimeFormatted = if (totalHours > 0) {
        "${totalHours}h ${totalMins}m"
    } else {
        "${totalMins}m"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            shape = MatrixShapes.Xl,
            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer),
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "FOCUS ANALYTICS",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MatrixColors.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Metrics Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MatrixShapes.Md,
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("TOTAL TIME", color = MatrixColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(totalTimeFormatted, color = MatrixColors.Primary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MatrixShapes.Md,
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SESSIONS", color = MatrixColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$completedCount", color = MatrixColors.Secondary, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "RECENT SESSIONS",
                    color = MatrixColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (sessions.isEmpty()) {
                    Text(
                        text = "No focus sessions recorded yet. Tap the focus icon on any task to start!",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sessions.take(10)) { session ->
                            Surface(
                                shape = MatrixShapes.Sm,
                                color = MatrixColors.SurfaceContainerLow,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.taskTitle,
                                            color = MatrixColors.TextHeader,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = AppIcons.Stopwatch,
                                                contentDescription = null,
                                                tint = MatrixColors.TextSecondary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (session.mode == "timer") "Timer" else "Stopwatch",
                                                color = MatrixColors.TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Text(
                                        text = session.formattedDuration,
                                        color = MatrixColors.Primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                ) {
                    Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
