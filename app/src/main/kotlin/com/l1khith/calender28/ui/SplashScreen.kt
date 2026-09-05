package com.l1khith.calender28.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MatrixSplashScreen(
    onEnterWorkspace: () -> Unit,
    onExitApp: () -> Unit = {}
) {
    BackHandler {
        onExitApp()
    }

    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF181B26),
                        Color(0xFF0C0D11),
                        Color(0xFF08090C)
                    ),
                    center = Offset.Unspecified,
                    radius = 1200f
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Center Branding & Logo Card
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rounded Squircle Card with App Icon
            Surface(
                modifier = Modifier
                    .size(136.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x80000000),
                        ambientColor = Color(0x40000000)
                    ),
                shape = RoundedCornerShape(26.dp),
                color = Color(0xFF15161C),
                border = BorderStroke(1.dp, Color(0xFF222430))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Folded Calendar Page Graphic with "28"
                    Box(
                        modifier = Modifier.size(width = 44.dp, height = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cornerRadius = 6.dp.toPx()
                            val foldSize = 12.dp.toPx()

                            // Page Outline with folded top-right corner
                            val pagePath = Path().apply {
                                moveTo(cornerRadius, 0f)
                                lineTo(w - foldSize, 0f)
                                lineTo(w, foldSize)
                                lineTo(w, h - cornerRadius)
                                quadraticTo(w, h, w - cornerRadius, h)
                                lineTo(cornerRadius, h)
                                quadraticTo(0f, h, 0f, h - cornerRadius)
                                lineTo(0f, cornerRadius)
                                quadraticTo(0f, 0f, cornerRadius, 0f)
                                close()
                            }

                            // Blue Page Gradient
                            drawPath(
                                path = pagePath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                                )
                            )

                            // Top-Right Fold Corner Flap
                            val foldPath = Path().apply {
                                moveTo(w - foldSize, 0f)
                                lineTo(w - foldSize, foldSize - 2f)
                                quadraticTo(w - foldSize, foldSize, w - foldSize + 2f, foldSize)
                                lineTo(w, foldSize)
                                close()
                            }
                            drawPath(
                                path = foldPath,
                                color = Color(0xFF1D4ED8)
                            )
                        }

                        // "28" Day Number
                        Text(
                            text = "28",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Calendar",
                        color = Color(0xFFE2E8F0),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // App Title
            Text(
                text = "Matrix 28",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = "The 13-Month Fixed Calendar & Focus\nEngine",
                color = Color(0xFF94A3B8),
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }

        // Bottom Action Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .alpha(contentAlpha.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onEnterWorkspace,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA8C7FA),
                    contentColor = Color(0xFF0A1C36)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Enter Workspace",
                        color = Color(0xFF0A1C36),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF0A1C36),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Designed for clarity and calm focus.",
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
