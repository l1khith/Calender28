package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * High-performance Canvas renderer drawing a subtle tinted background and a red dashed
 * indicator line across overlapping regions in the timeline.
 */
@Composable
fun OverlapOverlay(
    topPx: Float,
    heightPx: Float,
    color: Color = MatrixColors.Error,
    modifier: Modifier = Modifier
) {
    if (heightPx <= 0f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        // Tinted translucent background across overlap area
        drawRect(
            color = color.copy(alpha = 0.08f),
            topLeft = Offset(0f, topPx),
            size = Size(size.width, heightPx)
        )

        // Left accent dashed line
        val strokeWidth = 2.dp.toPx()
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        drawLine(
            color = color.copy(alpha = 0.8f),
            start = Offset(strokeWidth / 2, topPx),
            end = Offset(strokeWidth / 2, topPx + heightPx),
            strokeWidth = strokeWidth,
            pathEffect = dashEffect
        )
    }
}
