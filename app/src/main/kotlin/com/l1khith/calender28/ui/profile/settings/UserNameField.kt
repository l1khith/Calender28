package com.l1khith.calender28.ui.profile.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.ui.theme.MatrixColors

@Composable
fun UserNameField(
    currentName: String?,
    onNameChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember(currentName) { mutableStateOf(currentName ?: "") }
    var errorText by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    fun validateAndSave(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            errorText = null
            onNameChanged(null)
            return
        }

        if (trimmed.length > 20) {
            errorText = "Max 20 characters"
            return
        }

        // Check: no emoji-only strings (must contain at least one alphanumeric or letter)
        val hasLetterOrDigit = trimmed.any { it.isLetterOrDigit() }
        if (!hasLetterOrDigit) {
            errorText = "Name must contain letters or digits"
            return
        }

        errorText = null
        onNameChanged(trimmed)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Profile Name",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MatrixColors.TextHeader
            )
        )
        Text(
            text = "Used in your morning briefing and customizable top bar.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MatrixColors.TextSecondary
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                if (input.length <= 20) {
                    text = input
                    validateAndSave(input)
                }
            },
            singleLine = true,
            maxLines = 1,
            shape = RoundedCornerShape(8.dp),
            placeholder = {
                Text(
                    text = "Enter your name (optional)",
                    color = MatrixColors.TextSecondary.copy(alpha = 0.6f)
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    validateAndSave(text)
                    focusManager.clearFocus()
                }
            ),
            isError = errorText != null,
            supportingText = {
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = "${text.length}/20",
                        color = MatrixColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MatrixColors.Primary,
                unfocusedBorderColor = MatrixColors.OutlineVariant,
                focusedTextColor = MatrixColors.TextHeader,
                unfocusedTextColor = MatrixColors.TextHeader,
                cursorColor = MatrixColors.Primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
