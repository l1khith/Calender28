package com.l1khith.calender28.ui.topbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.l1khith.calender28.domain.model.TopBarSlotContent
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun TopBarTitle(
    slotContent: TopBarSlotContent,
    userName: String?,
    todayTaskCount: Int,
    pendingTaskCount: Int,
    modifier: Modifier = Modifier
) {
    val titleText = when (slotContent) {
        TopBarSlotContent.MATRIX28 -> "Matrix 28"
        TopBarSlotContent.USER_NAME -> {
            val cleanName = userName?.trim()
            if (!cleanName.isNullOrEmpty()) cleanName else "Matrix 28"
        }
        TopBarSlotContent.TASK_COUNT -> {
            when (todayTaskCount) {
                0 -> "No tasks today"
                1 -> "1 task today"
                else -> "$todayTaskCount tasks today"
            }
        }
        TopBarSlotContent.PENDING_TODOS -> {
            when (pendingTaskCount) {
                0 -> "All done ✓"
                1 -> "1 pending"
                else -> "$pendingTaskCount pending"
            }
        }
    }

    Text(
        text = titleText,
        style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MatrixColors.Primary
        ),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}
