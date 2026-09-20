package com.l1khith.calender28.ui.daydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.l1khith.calender28.data.Note
import java.util.Calendar

@Composable
fun NoteTimelineItem(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeLabel = remember(note.createdAt) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = note.createdAt
        }
        "%02d:%02d".format(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time label on left
        Text(
            text = timeLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(52.dp)
        )

        Spacer(Modifier.width(8.dp))

        // Note card
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Format badge (MD or TXT)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = note.format.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(
                            horizontal = 6.dp,
                            vertical = 2.dp
                        )
                    )
                }
                Spacer(Modifier.width(8.dp))
                val displayText = note.content.ifBlank { note.displayTitle }
                Text(
                    text = displayText.take(60).replace("\n", " "),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
