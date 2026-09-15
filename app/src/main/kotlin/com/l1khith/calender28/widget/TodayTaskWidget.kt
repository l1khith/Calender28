package com.l1khith.calender28.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.l1khith.calender28.MainActivity
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.TaskDatabase
import com.l1khith.calender28.utils.FixedCalendarHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ToggleTaskAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[TaskIdKey] ?: return
        val db = TaskDatabase(context)
        val task = db.getTaskById(taskId)
        if (task != null) {
            val updated = task.copy(isCompleted = if (task.completed) 0 else 1)
            db.updateTask(updated)
        }
    }

    companion object {
        val TaskIdKey = ActionParameters.Key<String>("task_id_key")
    }
}

class TodayTaskWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    companion object {
        private val widgetScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun updateWidget(context: Context) {
            widgetScope.launch {
                try {
                    TodayTaskWidget().updateAll(context)
                } catch (e: Exception) {
                    Log.e("TodayTaskWidget", "Failed to update widget instances", e)
                }
            }
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = TaskDatabase(context)
        val today = FixedCalendarHelper.fromTimestamp(System.currentTimeMillis())
        val todayStr = today.toString()
        val monthName = FixedCalendarHelper.getMonthName(today.month)
        val dayOfWeek = FixedCalendarHelper.getDayOfWeek(today)
        val todayTasks = try {
            db.getTasksForDate(todayStr)
        } catch (e: Exception) {
            Log.e("TodayTaskWidget", "Error fetching widget tasks for date $todayStr", e)
            emptyList()
        }

        val completedCount = todayTasks.count { it.completed }
        val totalCount = todayTasks.size

        provideContent {
            GlanceWidgetContent(
                context = context,
                dateTitle = if (today.isYearDay) "Sol Day" else if (today.isLeapDay) "Leap Day" else "${monthName.take(3)} ${today.day}",
                subtitleText = "$dayOfWeek • Month ${today.month}",
                cycleInfo = if (today.isYearDay) "Sol Leave Day" else "Day ${today.day} of 28",
                completedCount = completedCount,
                totalCount = totalCount,
                tasks = todayTasks
            )
        }
    }

    @Composable
    private fun GlanceWidgetContent(
        context: Context,
        dateTitle: String,
        subtitleText: String,
        cycleInfo: String,
        completedCount: Int,
        totalCount: Int,
        tasks: List<AppTask>
    ) {
        val size = LocalSize.current
        val isCompact = size.height < 120.dp || size.width < 220.dp
        val maxTaskDisplay = if (size.height > 200.dp) 6 else if (isCompact) 2 else 4

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(Color(0xFF18181B))
                .cornerRadius(12.dp)
                .padding(if (isCompact) 8.dp else 14.dp)
                .clickable(
                    actionStartActivity<MainActivity>()
                )
        ) {
            // HEADER ROW: DATE ACCENT & CYCLE BADGE
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = dateTitle.uppercase(),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFADC6FF)),
                            fontSize = if (isCompact) 16.sp else 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = subtitleText,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFE4E1E5)),
                            fontSize = if (isCompact) 10.sp else 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                // BADGE & STATS
                Box(
                    modifier = GlanceModifier
                        .background(Color(0xFF27272A))
                        .cornerRadius(10.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (totalCount > 0) "$completedCount/$totalCount • $cycleInfo" else cycleInfo,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF4D8EFF)),
                            fontSize = if (isCompact) 9.sp else 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(if (isCompact) 6.dp else 10.dp))

            // DIVIDER
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0xFF424754))
            ) {}

            Spacer(modifier = GlanceModifier.height(if (isCompact) 6.dp else 10.dp))

            // TASKS SECTION
            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks scheduled for today. Tap to open Calender28",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFC2C6D6)),
                        fontSize = if (isCompact) 11.sp else 12.sp
                    )
                )
            } else {
                for (task in tasks.take(maxTaskDisplay)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = GlanceModifier.padding(vertical = 2.dp)
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .padding(end = 6.dp)
                                .clickable(
                                    actionRunCallback<ToggleTaskAction>(
                                        actionParametersOf(ToggleTaskAction.TaskIdKey to task.id)
                                    )
                                )
                        ) {
                            Text(
                                text = if (task.completed) "✓ " else "• ",
                                style = TextStyle(
                                    color = ColorProvider(if (task.completed) Color(0xFF4D8EFF) else Color(0xFFADC6FF)),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = task.title,
                            maxLines = 1,
                            style = TextStyle(
                                color = ColorProvider(if (task.completed) Color(0xFFC2C6D6) else Color(0xFFE4E1E5)),
                                fontSize = if (isCompact) 11.sp else 13.sp,
                                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                            )
                        )
                    }
                }
            }
        }
    }
}

class TodayTaskWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTaskWidget()
}

