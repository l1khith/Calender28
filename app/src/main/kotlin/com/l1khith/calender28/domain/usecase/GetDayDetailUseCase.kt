package com.l1khith.calender28.domain.usecase

import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.data.RecurringTask
import com.l1khith.calender28.data.ScheduledAlarmDao
import com.l1khith.calender28.data.ScheduledAlarmEntity
import com.l1khith.calender28.repository.FocusRepository
import com.l1khith.calender28.repository.HabitRepository
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.utils.FixedCalendarHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

data class DayDetail(
    val dateStr: String,
    val timedTasks: List<AppTask>,
    val allDayTasks: List<AppTask>,
    val crossDayTasks: List<AppTask>,
    val recurringInstances: List<RecurringTask>,
    val habitReminders: List<Habit>,
    val focusSessions: List<FocusSession>,
    val scheduledAlarms: List<ScheduledAlarmEntity>
)

class GetDayDetailUseCase(
    private val taskRepo: TaskRepository,
    private val habitRepo: HabitRepository,
    private val focusRepo: FocusRepository,
    private val scheduledAlarmDao: ScheduledAlarmDao
) {
    suspend operator fun invoke(dateStr: String): DayDetail = withContext(Dispatchers.IO) {
        coroutineScope {
            val fixedDate = FixedCalendarHelper.parseDateStr(dateStr)
            val startOfDayMs = if (fixedDate != null) FixedCalendarHelper.toTimestamp(fixedDate, "00:00") else 0L
            val endOfDayMs = if (fixedDate != null) FixedCalendarHelper.toTimestamp(fixedDate, "23:59") else Long.MAX_VALUE

            val spanningDeferred = async { taskRepo.getTasksSpanningDate(dateStr) }
            val recurringDeferred = async {
                taskRepo.getAllRecurringTasks().filter { it.isActive && (it.endDate.isNullOrEmpty() || it.endDate >= dateStr) }
            }
            val habitsDeferred = async {
                habitRepo.getAllHabits().filter { !it.isPaused }
            }
            val focusDeferred = async {
                if (fixedDate != null) focusRepo.getSessionsInRange(startOfDayMs, endOfDayMs) else emptyList()
            }
            val alarmsDeferred = async {
                if (fixedDate != null) scheduledAlarmDao.getAlarmsInRange(startOfDayMs, endOfDayMs) else emptyList()
            }

            val spanningTasks = spanningDeferred.await()
            val timedTasks = spanningTasks.filter { !it.allDay && !it.spansMidnight }
            val allDayTasks = spanningTasks.filter { it.allDay }
            val crossDayTasks = spanningTasks.filter { it.spansMidnight }

            DayDetail(
                dateStr = dateStr,
                timedTasks = timedTasks,
                allDayTasks = allDayTasks,
                crossDayTasks = crossDayTasks,
                recurringInstances = recurringDeferred.await(),
                habitReminders = habitsDeferred.await(),
                focusSessions = focusDeferred.await(),
                scheduledAlarms = alarmsDeferred.await()
            )
        }
    }
}
