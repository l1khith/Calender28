package com.l1khith.calender28.ui.sparky

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.l1khith.calender28.R
import com.l1khith.calender28.data.EvolutionStage
import com.l1khith.calender28.data.SparkyMood

/**
 * Animated companion visual powered by Lottie compositions downloaded for Sparky.
 */
@Composable
fun SparkyAnimation(
    mood: SparkyMood,
    stage: EvolutionStage = EvolutionStage.BABY,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    speed: Float = 1.0f,
    equippedSkin: String? = null
) {
    val rawRes = when {
        mood == SparkyMood.EVOLVING -> R.raw.bird_egg_breaks
        stage == EvolutionStage.EGG && mood != SparkyMood.CELEBRATING -> R.raw.bird_egg_breaks
        mood == SparkyMood.CELEBRATING || mood == SparkyMood.PROUD -> R.raw.bird_success
        mood == SparkyMood.CALM -> R.raw.bird_dreaming
        mood == SparkyMood.TIRED || mood == SparkyMood.SAD -> R.raw.bird_tired
        else -> R.raw.bird_happy
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(rawRes))
    val iterations = if (mood == SparkyMood.EVOLVING) 1 else LottieConstants.IterateForever

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed,
        isPlaying = true
    )

    // Subtle natural breathing bounce
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

    Box(
        modifier = modifier
            .size(size)
            .scale(breathScale),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize()
        )
    }
}
