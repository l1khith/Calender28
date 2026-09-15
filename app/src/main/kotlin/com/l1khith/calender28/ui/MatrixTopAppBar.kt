package com.l1khith.calender28.ui

import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.SparkyMood
import com.l1khith.calender28.ui.sparky.SparkyAnimation
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.SoundEffectHelper

/**
 * Top application bar extracted from FixedCalendarApp for Single Responsibility and isolated recomposition.
 * Employs lambda-based graphicsLayer blocks for 60fps draw-phase animations without triggering layout recompositions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixTopAppBar(
    context: Context,
    profileNavIcon: ImageVector,
    overallStreak: Int,
    coinBalance: Int,
    enableSparky: Boolean,
    enableAnimations: Boolean,
    sparkyMood: SparkyMood,
    sparkyStage: com.l1khith.calender28.data.EvolutionStage,
    sparkyEquippedSkin: String?,
    onOpenProfile: () -> Unit,
    onOpenStreakInfo: () -> Unit,
    onOpenCoinStore: () -> Unit,
    onOpenSparkyDetail: () -> Unit,
    onOpenSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current.density
    val cardBackground = MatrixColors.SurfaceContainerHigh
    val borderSubtle = MatrixColors.OutlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MatrixColors.Surface)
    ) {
        TopAppBar(
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(cardBackground)
                        .border(BorderStroke(1.dp, borderSubtle), CircleShape)
                        .clickable { onOpenProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = profileNavIcon,
                        contentDescription = "Profile",
                        tint = Color(0xFFC2C6D6),
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Matrix 28",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MatrixColors.Primary
                    ),
                    maxLines = 1
                )
            },
            actions = {
                val downloadedSyncIcon = remember {
                    ImageVector.Builder(
                        name = "DownloadedSyncIcon",
                        defaultWidth = 32.dp,
                        defaultHeight = 37.dp,
                        viewportWidth = 32f,
                        viewportHeight = 37f
                    ).addPath(
                        pathData = PathParser().parsePathString(
                            "M9 22.3333V20.6667H11.2917L10.9583 20.375C10.2361 19.7361 9.72917 19.0069 9.4375 18.1875C9.14583 17.3681 9 16.5417 9 15.7083C9 14.1667 9.46181 12.7951 10.3854 11.5938C11.309 10.3924 12.5139 9.59722 14 9.20833V10.9583C13 11.3194 12.1944 11.934 11.5833 12.8021C10.9722 13.6701 10.6667 14.6389 10.6667 15.7083C10.6667 16.3333 10.7847 16.941 11.0208 17.5312C11.2569 18.1215 11.625 18.6667 12.125 19.1667L12.3333 19.375V17.3333H14V22.3333H9V22.3333M17.3333 22.125V20.375C18.3333 20.0139 19.1389 19.3993 19.75 18.5312C20.3611 17.6632 20.6667 16.6944 20.6667 15.625C20.6667 15 20.5486 14.3924 20.3125 13.8021C20.0764 13.2118 19.7083 12.6667 19.2083 12.1667L19 11.9583V14H17.3333V9H22.3333V10.6667H20.0417L20.375 10.9583C21.0556 11.6389 21.5521 12.3785 21.8646 13.1771C22.1771 13.9757 22.3333 14.7917 22.3333 15.625C22.3333 17.1667 21.8715 18.5382 20.9479 19.7396C20.0243 20.941 18.8194 21.7361 17.3333 22.125V22.125"
                        ).toNodes(),
                        fill = SolidColor(Color(0xFFC2C6D6))
                    ).build()
                }

                // Flame wiggle / flicker animation
                val flameTransition = rememberInfiniteTransition(label = "flame_wiggle")
                val animatedFlameRotation by flameTransition.animateFloat(
                    initialValue = -8f,
                    targetValue = 8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 180, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame_rot"
                )
                val animatedFlameScale by flameTransition.animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 240, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "flame_scale"
                )
                val flameRotation = if (enableAnimations) animatedFlameRotation else 0f
                val flameScale = if (enableAnimations) animatedFlameScale else 1f

                // Mini Sparky Avatar (left of Streak)
                if (enableSparky) {
                    Surface(
                        shape = CircleShape,
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(34.dp)
                            .clickable { onOpenSparkyDetail() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SparkyAnimation(
                                mood = sparkyMood,
                                stage = sparkyStage,
                                size = 28.dp,
                                equippedSkin = sparkyEquippedSkin
                            )
                        }
                    }
                }

                // Overall Streak Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable {
                            SoundEffectHelper.playFireSound(context)
                            onOpenStreakInfo()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Fire,
                            contentDescription = "Streak",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(16.dp)
                                .then(
                                    if (enableAnimations) {
                                        Modifier.graphicsLayer {
                                            rotationZ = flameRotation
                                            scaleX = flameScale
                                            scaleY = flameScale
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$overallStreak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MatrixColors.TextHeader
                        )
                    }
                }

                // Coin 3D Y-axis flip animation
                val coinFlipTransition = rememberInfiniteTransition(label = "coin_flip")
                val animatedCoinRotationY by coinFlipTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 180, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "coin_rot_y"
                )
                val coinRotationY = if (enableAnimations) animatedCoinRotationY else 0f

                // CalCoins Chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clickable {
                            SoundEffectHelper.playCoinSound(context)
                            onOpenCoinStore()
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = AppIcons.Coin,
                            contentDescription = "Coins",
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(16.dp)
                                .then(
                                    if (enableAnimations) {
                                        Modifier.graphicsLayer {
                                            rotationY = coinRotationY
                                            cameraDistance = 12f * density
                                        }
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$coinBalance",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MatrixColors.TextHeader
                        )
                    }
                }

                // Sync Icon Box
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(cardBackground)
                        .border(BorderStroke(1.dp, borderSubtle), CircleShape)
                        .clickable { onOpenSync() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = downloadedSyncIcon,
                        contentDescription = "Sync",
                        tint = Color(0xFFC2C6D6),
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MatrixColors.Surface,
                titleContentColor = Color(0xFF3B82F6)
            )
        )

        AnimatedSparkDivider(
            baseColor = Color(0xFF424754),
            sparkColor = MatrixColors.Primary,
            glowColor = MatrixColors.Secondary,
            height = 1.dp,
            reverseDirection = true,
            durationMillis = 4000,
            enableSparkle = enableAnimations
        )
    }
}
