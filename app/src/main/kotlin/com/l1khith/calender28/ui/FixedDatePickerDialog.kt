package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate

@Composable
fun FixedDatePickerDialog(
    initialDateStr: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val initialDate = remember(initialDateStr) {
        FixedCalendarHelper.parseDateStr(initialDateStr) ?: FixedCalendarHelper.currentFixedDate()
    }

    var year by remember { mutableIntStateOf(initialDate.year) }
    var month by remember { mutableIntStateOf(initialDate.month) }
    var day by remember { mutableIntStateOf(initialDate.day) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MatrixColors.Surface,
            border = BorderStroke(1.dp, MatrixColors.OutlineVariant),
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Date",
                    color = MatrixColors.TextHeader,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Selectors for Month, Day, Year
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (month < 13) month++ else month = 1 }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Next Month", tint = MatrixColors.Primary)
                        }
                        Text(
                            text = FixedCalendarHelper.getMonthName(month).take(3),
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "M$month",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        IconButton(onClick = { if (month > 1) month-- else month = 13 }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Prev Month", tint = MatrixColors.Primary)
                        }
                    }

                    // Day Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { if (day < 28) day++ else day = 1 }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Next Day", tint = MatrixColors.Primary)
                        }
                        Text(
                            text = "%02d".format(day),
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Day",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        IconButton(onClick = { if (day > 1) day-- else day = 28 }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Prev Day", tint = MatrixColors.Primary)
                        }
                    }

                    // Year Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { year++ }) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Next Year", tint = MatrixColors.Primary)
                        }
                        Text(
                            text = year.toString(),
                            color = MatrixColors.TextHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Year",
                            color = MatrixColors.TextSecondary,
                            fontSize = 11.sp
                        )
                        IconButton(onClick = { year-- }) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Prev Year", tint = MatrixColors.Primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MatrixColors.TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val selectedFixedDate = FixedDate(year, month, day)
                            onDateSelected(selectedFixedDate.toString())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MatrixColors.Primary)
                    ) {
                        Text("Select", color = MatrixColors.OnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
