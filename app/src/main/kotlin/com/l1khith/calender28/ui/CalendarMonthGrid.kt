package com.l1khith.calender28.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.l1khith.calender28.ui.theme.MatrixColors
import com.l1khith.calender28.ui.theme.MatrixShapes
import com.l1khith.calender28.utils.FixedCalendarHelper
import com.l1khith.calender28.utils.FixedDate

@Composable
fun MonthYearSelector(
    selectedDate: FixedDate,
    onDateChange: (FixedDate) -> Unit,
    primaryColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    fun navigateMonth(delta: Int) {
        var newM = selectedDate.month + delta
        var newY = selectedDate.year
        if (newM < 1) {
            newM = 13
            newY -= 1
        } else if (newM > 13) {
            newM = 1
            newY += 1
        }
        val newDay = if (selectedDate.day == 29 && newM != 6 && newM != 13) 28 else selectedDate.day
        val isLD = newM == 6 && newDay == 29
        val isYD = newM == 13 && newDay == 29
        onDateChange(FixedDate(newY, newM, newDay, isLeapDay = isLD, isYearDay = isYD))
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month Selector with Arrow Marks
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navigateMonth(-1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous Month",
                    tint = primaryColor
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = FixedCalendarHelper.getMonthName(selectedDate.month),
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Month ${selectedDate.month} of 13",
                    color = MatrixColors.TextSecondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { navigateMonth(1) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Month",
                    tint = primaryColor
                )
            }
        }

        // Year Selector with Arrow Marks
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                onDateChange(selectedDate.copy(year = selectedDate.year - 1))
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Prev Year",
                    tint = primaryColor
                )
            }
            Text(
                text = selectedDate.year.toString(),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            IconButton(onClick = {
                onDateChange(selectedDate.copy(year = selectedDate.year + 1))
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next Year",
                    tint = primaryColor
                )
            }
        }
    }
}

@Composable
fun CalendarMatrix(
    selectedDate: FixedDate,
    activeDates: Set<String>,
    taskCounts: Map<String, Int>,
    todayFixed: FixedDate,
    onDateSelect: (FixedDate) -> Unit,
    primaryColor: Color,
    orangeDotColor: Color,
    cardBg: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val weekDays = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val isLeap = FixedCalendarHelper.isLeapYear(selectedDate.year)
    val headerHeight = 38.dp
    val rowHeight = 52.dp
    val gridLineColor = borderColor

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MatrixShapes.Lg,
        colors = CardDefaults.cardColors(containerColor = MatrixColors.SurfaceContainerLow),
        border = BorderStroke(1.dp, gridLineColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val strokeWidth = 1.dp.toPx()
                        val colWidth = size.width / 7f
                        val headerHeightPx = headerHeight.toPx()
                        val rowHeightPx = rowHeight.toPx()

                        // ─── 1. Continuous Vertical Grid Lines (Top to Bottom) ───
                        for (col in 1..6) {
                            val x = col * colWidth
                            drawLine(
                                color = gridLineColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = strokeWidth
                            )
                        }

                        // ─── 2. Horizontal Divider below Weekday Header ───
                        drawLine(
                            color = gridLineColor,
                            start = Offset(0f, headerHeightPx),
                            end = Offset(size.width, headerHeightPx),
                            strokeWidth = strokeWidth
                        )

                        // ─── 3. Horizontal Dividers between Date Rows ─────
                        for (r in 1..3) {
                            val y = headerHeightPx + (r * rowHeightPx)
                            drawLine(
                                color = gridLineColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                    }
            ) {
                // Weekday Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (day in weekDays) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                textAlign = TextAlign.Center,
                                color = MatrixColors.TextSecondary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Date Grid (4 rows x 7 columns)
                for (row in 0 until 4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(rowHeight)
                    ) {
                        for (col in 1..7) {
                            val dayNum = row * 7 + col
                            val cellDate = FixedDate(selectedDate.year, selectedDate.month, dayNum)
                            val cellDateStr = cellDate.toString()
                            val taskCount = taskCounts[cellDateStr] ?: if (activeDates.contains(cellDateStr)) 1 else 0
                            val isCellSelected = selectedDate.month == cellDate.month && selectedDate.day == dayNum
                            val isCellToday = todayFixed.year == cellDate.year && todayFixed.month == cellDate.month && todayFixed.day == dayNum

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onDateSelect(cellDate) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (isCellSelected) Color(0xFF3B82F6) else Color.Transparent)
                                            .border(
                                                width = if (isCellToday && !isCellSelected) 1.dp else 0.dp,
                                                color = if (isCellToday && !isCellSelected) Color(0xFF3B82F6) else Color.Transparent,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            color = if (isCellSelected) Color.White else if (taskCount > 0) MatrixColors.TextHeader else MatrixColors.TextSecondary,
                                            fontWeight = if (isCellSelected || isCellToday || taskCount > 0) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    }

                                    if (taskCount > 0) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val dotsToShow = taskCount.coerceAtMost(3)
                                            for (i in 0 until dotsToShow) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                    .background(
                                                        if (i % 2 == 0) MatrixColors.Tertiary else MatrixColors.Secondary
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Special Days (Leap Day / Sol Day)
            if (isLeap && selectedDate.month == 6) {
                HorizontalDivider(color = gridLineColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                val leapDay = FixedDate(selectedDate.year, 6, 29, isLeapDay = true)
                val isLeapSelected = selectedDate.month == 6 && selectedDate.day == 29
                val isLeapToday = todayFixed.year == selectedDate.year && todayFixed.month == 6 && todayFixed.day == 29
                val hasTasks = activeDates.contains(leapDay.toString())

                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SpecialDayCard(
                        label = "Leap Day — Leave Day (June 29)",
                        isSelected = isLeapSelected,
                        isToday = isLeapToday,
                        hasTasks = hasTasks,
                        onClick = { onDateSelect(leapDay) },
                        primaryColor = primaryColor,
                        orangeDotColor = orangeDotColor,
                        textColor = textColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            if (selectedDate.month == 13) {
                HorizontalDivider(color = gridLineColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))
                val yearDay = FixedDate(selectedDate.year, 13, 29, isYearDay = true)
                val isYearSelected = selectedDate.month == 13 && selectedDate.day == 29
                val isYearToday = todayFixed.year == selectedDate.year && todayFixed.month == 13 && todayFixed.day == 29
                val hasTasks = activeDates.contains(yearDay.toString())

                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    SpecialDayCard(
                        label = "Sol Day — Leave Day (December 29)",
                        isSelected = isYearSelected,
                        isToday = isYearToday,
                        hasTasks = hasTasks,
                        onClick = { onDateSelect(yearDay) },
                        primaryColor = primaryColor,
                        orangeDotColor = orangeDotColor,
                        textColor = textColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
fun SpecialDayCard(
    label: String,
    isSelected: Boolean,
    isToday: Boolean,
    hasTasks: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    orangeDotColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor else Color.Black
        ),
        border = BorderStroke(
            width = if (isToday && !isSelected) 1.dp else 0.dp,
            color = if (isToday && !isSelected) primaryColor else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Color.White.copy(alpha = 0.2f) else Color(0xFF1E293B)
                ) {
                    Text(
                        text = "LEAVE DAY",
                        color = if (isSelected) Color.White else Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (hasTasks) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else orangeDotColor)
                )
            }
        }
    }
}
