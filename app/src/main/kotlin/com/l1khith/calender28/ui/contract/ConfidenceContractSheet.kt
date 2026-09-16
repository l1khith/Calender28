package com.l1khith.calender28.ui.contract

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.BetStatus
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.ui.theme.MatrixColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfidenceContractSheet(
    betStatus: BetStatus,
    onDismiss: () -> Unit,
    onPlaceBet: (ConfidenceTier) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTier by remember { mutableStateOf(ConfidenceTier.A) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MatrixColors.Surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Confidence Contract",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MatrixColors.TextHeader
                    )
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MatrixColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (betStatus) {
                is BetStatus.Active -> {
                    Text(
                        text = "Your contract for today is active!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${betStatus.tier.displayName} Tier",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MatrixColors.Primary
                                    )
                                )
                                Text(
                                    text = "+${betStatus.reward} / -${betStatus.penalty} coins",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MatrixColors.TextSecondary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val progress = if (betStatus.requiredToWin > 0) {
                                (betStatus.currentCompletedCount.toFloat() / betStatus.requiredToWin).coerceIn(0f, 1f)
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = MatrixColors.Primary,
                                trackColor = MatrixColors.SurfaceContainerHighest
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Completed: ${betStatus.currentCompletedCount} / ${betStatus.requiredToWin} required",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MatrixColors.TextHeader,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                if (betStatus.currentCompletedCount >= betStatus.requiredToWin) {
                                    Text(
                                        text = "Goal Reached! ✓",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MatrixColors.Primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                is BetStatus.ReadyToBet -> {
                    Text(
                        text = "Commit to completing your scheduled tasks today. Win bonus coins on success!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MatrixColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Tier Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConfidenceTier.entries.forEach { tier ->
                            val isSelected = tier == selectedTier
                            val required = tier.requiredTasksToWin(betStatus.taskCount)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MatrixColors.Primary.copy(alpha = 0.15f) else MatrixColors.SurfaceContainerHigh,
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTier = tier }
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = tier.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MatrixColors.Primary else MatrixColors.TextHeader
                                        )
                                    )
                                    Text(
                                        text = "${(tier.commitmentFraction * 100).toInt()}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) MatrixColors.Primary else MatrixColors.TextHeader
                                        )
                                    )
                                    Text(
                                        text = "$required / ${betStatus.taskCount} tasks",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+${tier.rewardCoins} / -${tier.penaltyCoins}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MatrixColors.TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            onPlaceBet(selectedTier)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = MatrixColors.Surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Place ${selectedTier.displayName} Bet (+${selectedTier.rewardCoins} coins)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                is BetStatus.Unavailable -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Unavailable",
                                tint = MatrixColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = "Betting Unavailable",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MatrixColors.TextHeader
                                    )
                                )
                                Text(
                                    text = betStatus.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MatrixColors.TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }

                is BetStatus.Evaluated -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MatrixColors.SurfaceContainerHigh,
                        border = BorderStroke(1.dp, MatrixColors.Primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (betStatus.won) "Contract Won! 🎉" else "Contract Lost",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MatrixColors.Primary
                                )
                            )
                            Text(
                                text = betStatus.message,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MatrixColors.TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
