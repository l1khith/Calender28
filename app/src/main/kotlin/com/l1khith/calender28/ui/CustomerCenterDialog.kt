package com.l1khith.calender28.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.l1khith.calender28.billing.RevenueCatManager
import com.l1khith.calender28.billing.SubscriptionManager
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import kotlinx.coroutines.launch

@Composable
fun CustomerCenterDialog(
    onDismiss: () -> Unit
) {
    val isProActive by SubscriptionManager.isProActive.collectAsState()
    val isPremium by RevenueCatManager.isPremium.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isRestoring by remember { mutableStateOf(false) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }

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
                            imageVector = Icons.Default.Person,
                            contentDescription = "Customer Center",
                            tint = MatrixColors.Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Customer Center",
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

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainer),
                    border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
                    shape = MatrixShapes.Md,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Account Status",
                            color = MatrixColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                isPremium -> "PRO SUBSCRIBER (ACTIVE)"
                                isProActive -> "PRO ACTIVE (CALCOINS UNLOCK)"
                                else -> "FREE USER"
                            },
                            color = if (isProActive) MatrixColors.Tertiary else MatrixColors.TextHeader,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = when {
                                isPremium -> "Verified via Google Play & RevenueCat"
                                isProActive -> "Unlocked via CalCoins balance"
                                else -> "Upgrade to Pro for ad-free and full features"
                            },
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (restoreMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = restoreMessage!!,
                        color = MatrixColors.Primary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isPremium) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/account/subscriptions")
                            )
                            context.startActivity(intent)
                        },
                        shape = MatrixShapes.Xl,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manage Google Play Subscription")
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            isRestoring = true
                            restoreMessage = null
                            coroutineScope.launch {
                                val res = RevenueCatManager.restore()
                                isRestoring = false
                                restoreMessage = when (res) {
                                    is RevenueCatManager.PurchaseResult.Success -> "Purchases restored successfully"
                                    is RevenueCatManager.PurchaseResult.Error -> res.message
                                    else -> "No active purchases found"
                                }
                            }
                        },
                        enabled = !isRestoring,
                        shape = MatrixShapes.Xl,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRestoring) "Restoring..." else "Restore Purchases")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", color = MatrixColors.TextSecondary)
                }
            }
        }
    }
}
