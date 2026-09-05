package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.FocusSession
import com.l1khith.calender28.repository.CoinRepository
import com.l1khith.calender28.repository.CoinRepositoryImpl
import com.l1khith.calender28.repository.FocusRepository
import com.l1khith.calender28.repository.FocusRepositoryImpl
import com.l1khith.calender28.service.FocusSessionManager
import com.l1khith.calender28.service.FocusState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusViewModel(
    application: Application,
    private val focusRepo: FocusRepository,
    private val coinRepository: CoinRepository
) : AndroidViewModel(application) {

    constructor(application: Application) : this(
        application,
        FocusRepositoryImpl(application.applicationContext),
        CoinRepositoryImpl(application.applicationContext)
    )

    private val context = application.applicationContext

    val focusState: StateFlow<FocusState> = FocusSessionManager.focusState

    val allSessions: StateFlow<List<FocusSession>> = focusRepo.getAllFocusSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFocusSeconds: StateFlow<Long> = focusRepo.getTotalFocusSecondsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val completedSessionCount: StateFlow<Int> = focusRepo.getCompletedSessionCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun openSetup(task: AppTask) {
        FocusSessionManager.openSetup(task)
    }

    fun closeSetup() {
        FocusSessionManager.closeSetup()
    }

    fun startFocus(task: AppTask, mode: String, durationMinutes: Int, pinScreen: Boolean = false) {
        FocusSessionManager.startFocus(context, task, mode, durationMinutes, pinScreen)
    }

    fun togglePause() {
        FocusSessionManager.togglePause(context)
    }

    fun stopOrCancel(markAsCancelled: Boolean = true) {
        FocusSessionManager.stopOrCancelFocus(context, markAsCancelled)
    }

    fun commitTaskDone(markTaskDone: Boolean = true) {
        // Award coins for completing a focus session
        val currentState = focusState.value
        if (currentState is FocusState.Completed) {
            val durationMinutes = (currentState.durationSeconds / 60).coerceAtLeast(1)
            val taskTitle = currentState.task.title
            viewModelScope.launch {
                coinRepository.rewardFocusSessionComplete(taskTitle, durationMinutes)
            }
        }
        FocusSessionManager.commitCompletedTask(context, markTaskDone)
    }

    fun focusAgain() {
        FocusSessionManager.restartSameTask(context)
    }
}
