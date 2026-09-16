package com.l1khith.calender28.ui.profile.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.TopBarSlotContent
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun TopBarSlotPicker(
    currentSlot: TopBarSlotContent,
    userName: String?,
    onSlotSelected: (TopBarSlotContent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Top Bar Left Title",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader
            )
        )
        Text(
            text = "Choose what appears in the top navigation bar title slot.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixColors.TextSecondary
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            TopBarSlotContent.entries.forEach { slot ->
                val isSelected = slot == currentSlot
                val subtitle = when (slot) {
                    TopBarSlotContent.MATRIX28 -> "Displays standard 'Matrix 28' header"
                    TopBarSlotContent.USER_NAME -> {
                        val name = userName?.trim()
                        if (!name.isNullOrEmpty()) "Displays '$name'" else "Displays 'Matrix 28' (no name set)"
                    }
                    TopBarSlotContent.TASK_COUNT -> "Displays count of today's scheduled tasks"
                    TopBarSlotContent.PENDING_TODOS -> "Displays count of today's uncompleted tasks"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MatrixColors.Primary.copy(alpha = 0.12f) else MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onSlotSelected(slot) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = slot.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MatrixColors.Primary else MatrixColors.TextHeader
                                )
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixColors.TextSecondary,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = MatrixColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
