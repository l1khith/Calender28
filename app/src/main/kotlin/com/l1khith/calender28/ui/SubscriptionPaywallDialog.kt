package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes

@Composable
fun SubscriptionPaywallDialog(
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit = {}
) {
    val isProActive by SubscriptionManager.isProActive.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MatrixShapes.Lg,
            colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Pro",
                            tint = MatrixColors.Secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Matrix 28 Pro",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MatrixColors.TextHeader
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MatrixColors.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isProActive) {
                    Surface(
                        shape = MatrixShapes.Md,
                        color = MatrixColors.TertiaryContainer.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, MatrixColors.Tertiary),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MatrixColors.Tertiary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pro Mode is Active! All themes unlocked & ads hidden.",
                                color = MatrixColors.Tertiary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Upgrade to Pro to unlock premium features and remove ads.",
                        color = MatrixColors.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text("Close", color = MatrixColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
