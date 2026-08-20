package com.l1khith.calender28.security

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes

private fun Context.findFragmentActivity(): FragmentActivity? {
    var ctx = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is FragmentActivity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

// ═══════════════════════════════════════════════════════════════════════════════
// 1. FULL-SCREEN SECURE APP LOCK OVERLAY
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun AppLockOverlay(
    onUnlockSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }

    // Pulsing shield animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scalePulse"
    )

    // Trigger biometric prompt automatically on display
    LaunchedEffect(Unit) {
        activity?.let { act ->
            AppLockManager.authenticate(
                activity = act,
                title = "Unlock Calender28",
                subtitle = "Verify your fingerprint, face, or PIN",
                onSuccess = onUnlockSuccess
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D10))
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Logo & Shield
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scalePulse),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MatrixColors.Primary.copy(alpha = 0.12f),
                    border = BorderStroke(2.dp, MatrixColors.Primary.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxSize()
                ) {}

                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = "App Locked",
                    tint = MatrixColors.Primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Calender28 is Locked",
                color = MatrixColors.TextHeader,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Biometric security is enabled. Verify your fingerprint, face, or device PIN to continue.",
                color = MatrixColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Primary Unlock Button
            Button(
                onClick = {
                    activity?.let { act ->
                        AppLockManager.authenticate(
                            activity = act,
                            title = "Unlock Calender28",
                            subtitle = "Verify your fingerprint, face, or PIN",
                            onSuccess = onUnlockSuccess
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(52.dp),
                shape = MatrixShapes.Lg,
                colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Fingerprint,
                        contentDescription = "Unlock",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "UNLOCK APP",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// 2. SECURITY & APP LOCK SETTINGS DIALOG
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
fun SecurityLockDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val isAppLockEnabled by AppLockManager.isAppLockEnabled.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var isToggling by remember { mutableStateOf(false) }

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
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MatrixColors.Primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Shield,
                                    contentDescription = "Security",
                                    tint = MatrixColors.Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Security & App Lock",
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MatrixColors.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Toggle Card
                Surface(
                    shape = MatrixShapes.Lg,
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric & PIN Lock",
                                color = MatrixColors.TextHeader,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Require fingerprint, face, or device PIN when opening Calender28",
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = isAppLockEnabled,
                            onCheckedChange = { checked ->
                                if (activity != null && !isToggling) {
                                    isToggling = true
                                    AppLockManager.toggleAppLock(
                                        activity = activity,
                                        enable = checked,
                                        scope = coroutineScope,
                                        onComplete = {
                                            isToggling = false
                                        }
                                    )
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = MatrixColors.Primary,
                                uncheckedThumbColor = MatrixColors.TextSecondary,
                                uncheckedTrackColor = MatrixColors.SurfaceContainerLow
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Status Badge
                Surface(
                    shape = MatrixShapes.Md,
                    color = if (isAppLockEnabled) Color(0xFF10B981).copy(alpha = 0.15f) else MatrixColors.SurfaceContainerLow,
                    border = BorderStroke(1.dp, if (isAppLockEnabled) Color(0xFF10B981).copy(alpha = 0.4f) else MatrixColors.OutlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isAppLockEnabled) "🛡️ Status: Protected" else "⚠️ Status: Not Protected",
                            color = if (isAppLockEnabled) Color(0xFF10B981) else MatrixColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isAppLockEnabled) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Lock Button
                    OutlinedButton(
                        onClick = {
                            AppLockManager.lockNow()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatrixShapes.Md,
                        border = BorderStroke(1.dp, MatrixColors.Primary)
                    ) {
                        Text(
                            text = "🔒 Lock & Test Now",
                            color = MatrixColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md,
                    colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
