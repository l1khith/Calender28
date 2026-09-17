package com.l1khith.calender28.ui.conflicts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.FreeSlot
import com.l1khith.calender28.ui.theme.MatrixColors

/**
 * Clickable chip presenting a candidate free slot suggestion.
 */
@Composable
fun SlotSuggestionChip(
    slot: FreeSlot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MatrixColors.Primary.copy(alpha = 0.12f))
            .border(1.dp, MatrixColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = slot.displayRange,
            color = MatrixColors.Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
