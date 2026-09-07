package com.l1khith.calender28.ui.sparky

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.l1khith.calender28.data.EvolutionStage
import com.l1khith.calender28.data.PersonalityTrait
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.SoundEffectHelper
import com.l1khith.calender28.viewmodel.SparkyViewModel

@Composable
fun SparkyHomeCard(
    sparkyViewModel: SparkyViewModel,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sparkyState by sparkyViewModel.sparkyState.collectAsStateWithLifecycle()
    val currentMood by sparkyViewModel.currentMood.collectAsStateWithLifecycle()
    val moodMessage by sparkyViewModel.moodMessage.collectAsStateWithLifecycle()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MatrixShapes.Lg)
            .clickable {
                SoundEffectHelper.playCoinSound(context)
                onOpenDetail()
            },
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Sparky Avatar + Name + Personality Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MatrixColors.PrimaryContainer.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val stageIcon = when (sparkyState.stage) {
                            EvolutionStage.EGG -> AppIcons.Egg
                            EvolutionStage.LEGEND -> AppIcons.SparkyCrown
                            else -> AppIcons.Sparky
                        }
                        Icon(
                            imageVector = stageIcon,
                            contentDescription = "Companion Stage",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "${sparkyState.name} (${sparkyState.stage.displayName})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MatrixColors.TextHeader,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Level ${sparkyState.level} • ${sparkyState.currentLevelXp}/100 XP",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MatrixColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Personality Chips (Zero Emoji - Vector Icons only)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sparkyState.dominantTraits.take(2).forEach { trait ->
                        PersonalityBadge(trait = trait)
                    }
                }
            }

            // Central Area: Interactive Animated Character + Conversational Bubble
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive Sparky Animation
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clickable {
                            SoundEffectHelper.playFireSound(context)
                            sparkyViewModel.interactWithSparky()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    SparkyAnimation(
                        mood = currentMood,
                        stage = sparkyState.stage,
                        size = 88.dp,
                        equippedSkin = sparkyState.equippedSkin
                    )
                }

                // Speech Bubble with Mood Dialogue
                Surface(
                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp),
                    color = MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                        AnimatedContent(
                            targetState = moodMessage,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "speech_bubble"
                        ) { message ->
                            Text(
                                text = "\"$message\"",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MatrixColors.TextHeader,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Mood: ${currentMood.label}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MatrixColors.Primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = "Tap",
                                    tint = MatrixColors.TextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Tap to interact",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MatrixColors.TextSecondary,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Progress & Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Streak & Stats with Vector Icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AppIcons.Fire,
                            contentDescription = "Streak",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${sparkyState.streakRecord}-day best",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MatrixColors.Secondary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Habits",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${sparkyState.totalHabitsCompleted} habits",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MatrixColors.TextSecondary,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // "Open Profile" chevron
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MatrixColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Details",
                        tint = MatrixColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Level XP Progress Bar
            LinearProgressIndicator(
                progress = { sparkyState.progressToNextLevel },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MatrixColors.Primary,
                trackColor = MatrixColors.SurfaceContainerHigh
            )
        }
    }
}

/**
 * Clean vector chip for personality traits with ZERO text emojis.
 */
@Composable
fun PersonalityBadge(trait: PersonalityTrait) {
    val (icon, color) = when (trait) {
        PersonalityTrait.CALM -> Icons.Default.SelfImprovement to Color(0xFF38BDF8)
        PersonalityTrait.ENERGETIC -> Icons.Default.Bolt to Color(0xFFF59E0B)
        PersonalityTrait.FRIENDLY -> Icons.Default.Favorite to Color(0xFFF43F5E)
        PersonalityTrait.CURIOUS -> Icons.Default.Search to Color(0xFFA855F7)
        PersonalityTrait.RESILIENT -> Icons.Default.Shield to Color(0xFF10B981)
        PersonalityTrait.WISE -> Icons.Default.Psychology to Color(0xFF6366F1)
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(0.8.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = trait.displayName,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
