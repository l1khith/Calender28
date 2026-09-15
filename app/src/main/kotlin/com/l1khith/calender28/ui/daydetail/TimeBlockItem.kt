package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun TimeBlockItem(
    task: AppTask,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier,
    conflictBadgeType: ConflictBadgeType? = null
) {
    val accentColor = when (task.priority) {
        3 -> MatrixColors.Error
        2 -> MatrixColors.Secondary
        else -> MatrixColors.Primary
    }

    val isCompleted = task.completed
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val titleColor = if (isCompleted) MatrixColors.TextSecondary else MatrixColors.TextHeader

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .border(
                width = 1.dp,
                color = if (conflictBadgeType != null) MatrixColors.Error else accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left solid accent bar (4dp)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = task.title,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = textDecoration,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (conflictBadgeType != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        ConflictBadge(type = conflictBadgeType)
                    }
                }

                if (task.formattedTimeRange.isNotEmpty()) {
                    Text(
                        text = task.formattedTimeRange,
                        color = MatrixColors.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }

                if (!task.description.isNullOrEmpty()) {
                    Text(
                        text = task.description,
                        color = MatrixColors.TextSecondary.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = "Toggle Complete",
                    tint = if (isCompleted) MatrixColors.Primary else MatrixColors.TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
