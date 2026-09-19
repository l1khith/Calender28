package com.l1khith.calender28.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes

@Composable
fun UpgradeDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = MatrixColors.Primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixColors.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                shape = MatrixShapes.Md,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixColors.Primary,
                    contentColor = Color.Black
                )
            ) {
                Text("Upgrade to Pro", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MatrixColors.TextSecondary
                )
            ) {
                Text("Not now")
            }
        },
        shape = MatrixShapes.Lg,
        containerColor = MatrixColors.SurfaceContainerHigh
    )
}
