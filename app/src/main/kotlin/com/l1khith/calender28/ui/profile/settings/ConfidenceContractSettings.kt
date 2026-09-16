package com.l1khith.calender28.ui.profile.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.domain.model.ConfidenceTier
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun ConfidenceContractSettings(
    selectedTier: ConfidenceTier,
    streak: Int,
    lossStreak: Int,
    onTierSelected: (ConfidenceTier) -> Unit,
    modifier: Modifier = Modifier
) {
    val isRecoveryDay = lossStreak >= 3

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Confidence Contract",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MatrixColors.TextHeader
                )
            )

            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MatrixColors.Primary.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, MatrixColors.Primary.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Streak",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$streak-day streak",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MatrixColors.Primary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }
        }

        Text(
            text = "Back your daily task goals with coins. Commit to a tier and stick to it.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixColors.TextSecondary
            ),
            modifier = Modifier.padding(bottom = 10.dp)
        )

        if (isRecoveryDay) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Recovery",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Recovery Day active after 3 losses. Take a breather today!",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MatrixColors.TextHeader,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }

        // Tier selection cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ConfidenceTier.entries.forEach { tier ->
                val isSelected = tier == selectedTier
                val commitmentText = "${(tier.commitmentFraction * 100).toInt()}%"

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MatrixColors.Primary.copy(alpha = 0.15f) else MatrixColors.SurfaceContainerHigh,
                    border = BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) MatrixColors.Primary else MatrixColors.OutlineVariant
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onTierSelected(tier) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = tier.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MatrixColors.Primary else MatrixColors.TextHeader,
                                fontSize = 13.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = commitmentText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MatrixColors.Primary else MatrixColors.TextHeader,
                                fontSize = 16.sp
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

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Milestones: +25 coins at 3 wins, +75 at 5 wins, +200 & Brave badge at 7 wins.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixColors.TextSecondary.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        )
    }
}
