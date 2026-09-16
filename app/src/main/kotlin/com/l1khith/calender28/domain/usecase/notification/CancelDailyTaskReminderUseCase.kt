package com.l1khith.calender28.domain.usecase.notification

import com.l1khith.calender28.repository.UserPreferencesRepository
import com.l1khith.calender28.service.AlarmScheduler

class CancelDailyTaskReminderUseCase(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke() {
        userPreferencesRepository.updateDailyReminderEnabled(false)
        alarmScheduler.cancelDailyTaskReminder()
    }
}
