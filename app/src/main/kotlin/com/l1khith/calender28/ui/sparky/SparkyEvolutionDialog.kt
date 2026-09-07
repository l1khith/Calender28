package com.l1khith.calender28.ui.sparky

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.l1khith.calender28.data.EvolutionStage
import com.l1khith.calender28.data.SparkyMood
import com.l1khith.calender28.ui.theme.AppIcons
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes

@Composable
fun SparkyEvolutionDialog(
    newStage: EvolutionStage,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MatrixShapes.Xl,
                colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
                border = BorderStroke(2.dp, MatrixColors.Primary)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MatrixColors.PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AppIcons.SparkyCrown,
                            contentDescription = null,
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text = "EVOLUTION COMPLETE!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = MatrixColors.Primary,
                            letterSpacing = 1.sp
                        )
                    )

                    // Lottie animation of egg break / evolution
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SparkyAnimation(
                            mood = SparkyMood.EVOLVING,
                            stage = newStage,
                            size = 160.dp
                        )
                    }

                    Text(
                        text = "Sparky has evolved into the ${newStage.displayName} stage!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MatrixColors.SecondaryContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MatrixColors.Secondary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = AppIcons.Coin,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Evolution Bonus: +100 CalCoins!",
                                color = MatrixColors.Secondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MatrixShapes.Md,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatrixColors.Primary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Continue with ${newStage.displayName} Sparky",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
