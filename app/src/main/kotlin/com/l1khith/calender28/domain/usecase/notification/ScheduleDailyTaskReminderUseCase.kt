package com.l1khith.calender28.domain.usecase.notification

import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.service.AlarmScheduler

class ScheduleDailyTaskReminderUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(hour: Int, minute: Int): Boolean {
        userPreferencesRepository.updateDailyReminderTime(hour, minute)
        userPreferencesRepository.updateDailyReminderEnabled(true)
        return alarmScheduler.scheduleDailyTaskReminder(hour, minute)
    }
}
