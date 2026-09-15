package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.ConflictSeverity
import com.l1khith.calender28.ui.theme.MatrixColors

enum class ConflictBadgeType {
    HARD,
    BACK_TO_BACK,
    SAME_SLOT,
    ALL_DAY_INFO,
    CROSS_DAY
}

@Composable
fun ConflictBadge(
    type: ConflictBadgeType,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (type) {
        ConflictBadgeType.HARD -> Triple(MatrixColors.Error.copy(alpha = 0.2f), MatrixColors.Error, "⚠️ Overlap")
        ConflictBadgeType.CROSS_DAY -> Triple(MatrixColors.Error.copy(alpha = 0.2f), MatrixColors.Error, "⚠️ Cross-Day")
        ConflictBadgeType.BACK_TO_BACK -> Triple(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFD97706), "⟷ No Break")
        ConflictBadgeType.SAME_SLOT -> Triple(Color(0xFFF59E0B).copy(alpha = 0.2f), Color(0xFFD97706), "Stacked")
        ConflictBadgeType.ALL_DAY_INFO -> Triple(MatrixColors.Primary.copy(alpha = 0.15f), MatrixColors.Primary, "ℹ️ All-Day")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
    }
}
