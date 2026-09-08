package com.l1khith.calender28.ui.sparky

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airbnb.lottie.compose.*
import com.l1khith.calender28.R
import com.l1khith.calender28.data.EvolutionStage
import com.l1khith.calender28.data.SparkyMood

/**
 * Animated companion visual powered by Lottie compositions.
 */
@Composable
fun SparkyAnimation(
    mood: SparkyMood,
    stage: EvolutionStage = EvolutionStage.BABY,
    modifier: Modifier = Modifier,
    size: Dp? = 100.dp,
    speed: Float = 1.0f,
    iterations: Int = LottieConstants.IterateForever,
    equippedSkin: String? = null
) {
    val enableAnimations by com.l1khith.calender28.utils.AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()

    if (!enableAnimations) {
        val boxModifier = if (size != null) modifier.size(size) else modifier
        Box(
            modifier = boxModifier,
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                imageVector = com.l1khith.calender28.ui.theme.AppIcons.Sparky,
                contentDescription = "Sparky",
                tint = com.l1khith.calender28.ui.theme.MatrixColors.Primary,
                modifier = Modifier.size((size ?: 40.dp) * 0.7f)
            )
        }
        return
    }

    val rawRes = when (mood) {
        SparkyMood.CELEBRATING -> {
            if (size == null) R.raw.confetti else R.raw.sparky_happy
        }
        SparkyMood.TIRED, SparkyMood.SAD, SparkyMood.CALM -> R.raw.sparky_sleepy
        SparkyMood.HAPPY, SparkyMood.IDLE, SparkyMood.PROUD, SparkyMood.ENERGETIC, SparkyMood.CURIOUS, SparkyMood.EVOLVING -> R.raw.sparky_happy
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    val playIterations = iterations

    val effectiveSpeed = when {
        speed != 1.0f -> speed
        mood == SparkyMood.CALM -> 0.8f
        mood == SparkyMood.TIRED || mood == SparkyMood.SAD -> 0.85f
        mood == SparkyMood.ENERGETIC -> 1.25f
        else -> 1.0f
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = playIterations,
        speed = effectiveSpeed,
        isPlaying = true
    )

    // Subtle natural breathing bounce for avatars
    val infiniteTransition = rememberInfiniteTransition(label = "sparky_breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    val boxModifier = if (size != null) {
        modifier
            .size(size)
            .scale(breathScale)
    } else {
        modifier
    }

    Box(
        modifier = boxModifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * Full-screen or container celebration confetti animation.
 */
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    contentScale: ContentScale = ContentScale.Crop
) {
    val enableAnimations by com.l1khith.calender28.utils.AppSettingsManager.enableAnimations.collectAsStateWithLifecycle()
    if (!enableAnimations) return

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.confetti))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
    }
}
