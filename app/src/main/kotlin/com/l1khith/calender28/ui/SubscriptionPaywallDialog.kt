package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
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
    val coroutineScope = rememberCoroutineScope()

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
                val isDevMode = remember { SubscriptionManager.isDevModeActive() }

                if (isDevMode) {
                    Surface(
                        shape = MatrixShapes.Sm,
                        color = Color(0xFF10B981).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "DEV MODE — Pro features active without purchase",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

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
                            text = "Matrix 28 Pro (Testing Track)",
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
                        Text(
                            text = "🎉 Pro Mode is Active! All themes unlocked & ads hidden.",
                            color = MatrixColors.Tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Text(
                        text = "Closed Testing Track Mode\nUnlock Premium Features & Remove Ads locally.",
                        color = MatrixColors.TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        SubscriptionManager.toggleProMode(coroutineScope)
                        onPurchaseSuccess()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isProActive) MatrixColors.SurfaceContainerHigh else MatrixColors.Primary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MatrixShapes.Md
                ) {
                    Text(
                        text = if (isProActive) "Deactivate Pro Mode" else "Activate Pro Mode (Testing Phase)",
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onDismiss) {
                    Text("Close", color = MatrixColors.TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

