package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.ui.theme.MatrixColors
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun NowIndicator(
    hourRowHeight: Dp,
    modifier: Modifier = Modifier,
    color: Color = MatrixColors.Error
) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }

    // Update every minute efficiently without continuous recomposition
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Calendar.getInstance()
            delay(60_000L)
        }
    }

    val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
    val currentMinute = currentTime.get(Calendar.MINUTE)
    val minuteFraction = currentMinute / 60f
    val totalHoursOffset = currentHour + minuteFraction

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
    ) {
        val circleRadius = 4.dp.toPx()
        // Draw little dot on the left axis
        drawCircle(
            color = color,
            radius = circleRadius,
            center = Offset(circleRadius, size.height / 2)
        )

        // Draw horizontal red time line across timeline
        drawLine(
            color = color,
            start = Offset(circleRadius * 2, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 2.dp.toPx()
        )
    }
}
