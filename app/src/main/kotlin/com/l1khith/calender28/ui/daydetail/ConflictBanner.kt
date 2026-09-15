package com.l1khith.calender28.ui.daydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun ConflictBanner(
    conflictCount: Int,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = conflictCount > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MatrixColors.Error.copy(alpha = 0.12f))
                .border(1.dp, MatrixColors.Error.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .clickable(onClick = onReviewClick)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚠️ $conflictCount scheduling conflict${if (conflictCount > 1) "s" else ""} found",
                        color = MatrixColors.TextHeader,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tap to review and automatically resolve",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }

                FilledTonalButton(
                    onClick = onReviewClick,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MatrixColors.Error.copy(alpha = 0.25f),
                        contentColor = MatrixColors.TextHeader
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Resolve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
