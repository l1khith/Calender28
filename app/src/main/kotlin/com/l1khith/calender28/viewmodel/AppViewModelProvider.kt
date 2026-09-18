package com.l1khith.calender28.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.l1khith.calender28.MatrixApplication

/**
 * Provides factory methods for creating ViewModels with their abstract dependencies injected.
 */
object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = matrixApplication()
            FixedCalendarViewModel(
                application = app,
                taskRepository = app.container.taskRepository,
                habitRepository = app.container.habitRepository,
                coinRepository = app.container.coinRepository,
                getBetStatusUseCase = app.container.getBetStatusUseCase,
                placeBetUseCase = app.container.placeBetUseCase,
                evaluateBetUseCase = app.container.evaluateBetUseCase
            )
        }
        initializer {
            val app = matrixApplication()
            CoinViewModel(
                application = app,
                coinRepository = app.container.coinRepository
            )
        }
        initializer {
            val app = matrixApplication()
            FocusViewModel(
                application = app,
                focusRepo = app.container.focusRepository,
                coinRepository = app.container.coinRepository
            )
        }
        initializer {
            val app = matrixApplication()
            SparkyViewModel(
                application = app,
                sparkyRepository = app.container.sparkyRepository
            )
        }
        initializer {
            val app = matrixApplication()
            NotesViewModel(
                application = app,
                noteRepository = app.container.noteRepository,
                generateDefaultNoteTitleUseCase = app.container.generateDefaultNoteTitleUseCase
            )
        }
        initializer {
            val app = matrixApplication()
            CustomizeNavViewModel(
                application = app,
                navRepository = app.container.navPreferencesRepository
            )
        }
        initializer {
            val app = matrixApplication()
            com.l1khith.calender28.ui.daydetail.DayDetailViewModel(
                application = app,
                getDayDetailUseCase = app.container.getDayDetailUseCase,
                getDayConflictsUseCase = app.container.getDayConflictsUseCase,
                taskRepository = app.container.taskRepository,
                resolveConflictUseCase = app.container.resolveConflictUseCase,
                suggestFreeSlotsUseCase = app.container.suggestFreeSlotsUseCase
            )
        }
        initializer {
            val app = matrixApplication()
            GraphViewModel(
                application = app,
                noteRepository = app.container.noteRepository,
                buildGraphUseCase = app.container.buildGraphUseCase,
                buildBatchGraphUseCase = app.container.buildBatchGraphUseCase
            )
        }
    }
}

/**
 * Extension function to retrieve [MatrixApplication] from [CreationExtras].
 */
fun CreationExtras.matrixApplication(): MatrixApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatrixApplication)
