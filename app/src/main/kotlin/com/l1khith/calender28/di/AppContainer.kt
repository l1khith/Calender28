package com.l1khith.calender28.di

import android.content.Context
import com.l1khith.calender28.data.RoomTaskDatabase
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.CoinRepositoryImpl
import com.l1khith.calender28.repository.FocusRepository
import com.l1khith.calender28.repository.FocusRepositoryImpl
import com.l1khith.calender28.repository.HabitRepository
import com.l1khith.calender28.repository.HabitRepositoryImpl
import com.l1khith.calender28.repository.NoteRepository
import com.l1khith.calender28.repository.NoteRepositoryImpl
import com.l1khith.calender28.repository.TaskRepository
import com.l1khith.calender28.repository.TaskRepositoryImpl
import com.l1khith.calender28.repository.UserPreferencesRepository

/**
 * Dependency container interface defining application-wide singletons.
 */
interface AppContainer {
    val database: RoomTaskDatabase
    val taskRepository: TaskRepository
    val habitRepository: HabitRepository
    val coinRepository: CoinRepository
    val focusRepository: FocusRepository
    val userPreferencesRepository: UserPreferencesRepository
    val sparkyRepository: com.l1khith.calender28.repository.SparkyRepository
    val noteRepository: com.l1khith.calender28.repository.NoteRepository
    val navPreferencesRepository: com.l1khith.calender28.repository.NavPreferencesRepository
    val appLockManager: com.l1khith.calender28.security.AppLockManager
    val appSettingsManager: com.l1khith.calender28.utils.AppSettingsManager
    val subscriptionManager: com.l1khith.calender28.billing.SubscriptionManager
    val themeManager: com.l1khith.calender28.ui.theme.ThemeManager
    val getDayDetailUseCase: com.l1khith.calender28.domain.usecase.GetDayDetailUseCase
    val detectTaskConflictsUseCase: com.l1khith.calender28.domain.usecase.DetectTaskConflictsUseCase
    val getDayConflictsUseCase: com.l1khith.calender28.domain.usecase.GetDayConflictsUseCase
    val placeBetUseCase: com.l1khith.calender28.domain.usecase.bet.PlaceBetUseCase
    val evaluateBetUseCase: com.l1khith.calender28.domain.usecase.bet.EvaluateBetUseCase
    val getBetStatusUseCase: com.l1khith.calender28.domain.usecase.bet.GetBetStatusUseCase
    val scheduleDailyTaskReminderUseCase: com.l1khith.calender28.domain.usecase.notification.ScheduleDailyTaskReminderUseCase
    val cancelDailyTaskReminderUseCase: com.l1khith.calender28.domain.usecase.notification.CancelDailyTaskReminderUseCase
    val generateDefaultNoteTitleUseCase: com.l1khith.calender28.domain.usecase.notes.GenerateDefaultNoteTitleUseCase
}

/**
 * Default implementation of [AppContainer] managing thread-safe singletons on applicationContext.
 */
class DefaultAppContainer(private val context: Context) : AppContainer {

    private val appContext: Context = context.applicationContext

    override val database: RoomTaskDatabase by lazy {
        RoomTaskDatabase.getInstance(appContext)
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository.getInstance(appContext)
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepositoryImpl(
            taskDao = database.taskDao(),
            recurringTaskDao = database.recurringTaskDao(),
            serviceAlarmScheduler = com.l1khith.calender28.service.AlarmScheduler(appContext)
        )
    }

    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitEntryDao = database.habitEntryDao(),
            alarmScheduler = com.l1khith.calender28.service.AlarmScheduler(appContext)
        )
    }

    override val coinRepository: CoinRepository by lazy {
        CoinRepositoryImpl(
            coinDao = database.coinDao(),
            userPrefsRepo = userPreferencesRepository
        )
    }

    override val focusRepository: FocusRepository by lazy {
        FocusRepositoryImpl(
            focusSessionDao = database.focusSessionDao(),
            taskDao = database.taskDao()
        )
    }

    override val sparkyRepository: com.l1khith.calender28.repository.SparkyRepository by lazy {
        com.l1khith.calender28.repository.SparkyRepositoryImpl(
            sparkyDao = database.sparkyDao(),
            coinRepository = coinRepository
        )
    }

    override val noteRepository: com.l1khith.calender28.repository.NoteRepository by lazy {
        com.l1khith.calender28.repository.NoteRepositoryImpl(database.noteDao())
    }

    override val navPreferencesRepository: com.l1khith.calender28.repository.NavPreferencesRepository by lazy {
        com.l1khith.calender28.repository.NavPreferencesRepositoryImpl(userPreferencesRepository)
    }

    override val appLockManager: com.l1khith.calender28.security.AppLockManager
        get() = com.l1khith.calender28.security.AppLockManager

    override val appSettingsManager: com.l1khith.calender28.utils.AppSettingsManager
        get() = com.l1khith.calender28.utils.AppSettingsManager

    override val subscriptionManager: com.l1khith.calender28.billing.SubscriptionManager
        get() = com.l1khith.calender28.billing.SubscriptionManager

    override val themeManager: com.l1khith.calender28.ui.theme.ThemeManager
        get() = com.l1khith.calender28.ui.theme.ThemeManager

    override val getDayDetailUseCase: com.l1khith.calender28.domain.usecase.GetDayDetailUseCase by lazy {
        com.l1khith.calender28.domain.usecase.GetDayDetailUseCase(
            taskRepo = taskRepository,
            habitRepo = habitRepository,
            focusRepo = focusRepository,
            scheduledAlarmDao = database.scheduledAlarmDao()
        )
    }

    override val detectTaskConflictsUseCase: com.l1khith.calender28.domain.usecase.DetectTaskConflictsUseCase by lazy {
        com.l1khith.calender28.domain.usecase.DetectTaskConflictsUseCase(taskRepository)
    }

    override val getDayConflictsUseCase: com.l1khith.calender28.domain.usecase.GetDayConflictsUseCase by lazy {
        com.l1khith.calender28.domain.usecase.GetDayConflictsUseCase(taskRepository)
    }

    private val serviceAlarmScheduler: com.l1khith.calender28.service.AlarmScheduler by lazy {
        com.l1khith.calender28.service.AlarmScheduler(appContext)
    }

    override val placeBetUseCase: com.l1khith.calender28.domain.usecase.bet.PlaceBetUseCase by lazy {
        com.l1khith.calender28.domain.usecase.bet.PlaceBetUseCase(
            taskRepository = taskRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    override val evaluateBetUseCase: com.l1khith.calender28.domain.usecase.bet.EvaluateBetUseCase by lazy {
        com.l1khith.calender28.domain.usecase.bet.EvaluateBetUseCase(
            taskRepository = taskRepository,
            coinRepository = coinRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    override val getBetStatusUseCase: com.l1khith.calender28.domain.usecase.bet.GetBetStatusUseCase by lazy {
        com.l1khith.calender28.domain.usecase.bet.GetBetStatusUseCase(
            taskRepository = taskRepository,
            userPreferencesRepository = userPreferencesRepository
        )
    }

    override val scheduleDailyTaskReminderUseCase: com.l1khith.calender28.domain.usecase.notification.ScheduleDailyTaskReminderUseCase by lazy {
        com.l1khith.calender28.domain.usecase.notification.ScheduleDailyTaskReminderUseCase(
            userPreferencesRepository = userPreferencesRepository,
            alarmScheduler = serviceAlarmScheduler
        )
    }

    override val cancelDailyTaskReminderUseCase: com.l1khith.calender28.domain.usecase.notification.CancelDailyTaskReminderUseCase by lazy {
        com.l1khith.calender28.domain.usecase.notification.CancelDailyTaskReminderUseCase(
            userPreferencesRepository = userPreferencesRepository,
            alarmScheduler = serviceAlarmScheduler
        )
    }

    override val generateDefaultNoteTitleUseCase: com.l1khith.calender28.domain.usecase.notes.GenerateDefaultNoteTitleUseCase by lazy {
        com.l1khith.calender28.domain.usecase.notes.GenerateDefaultNoteTitleUseCase(
            noteRepository = noteRepository
        )
    }
}
