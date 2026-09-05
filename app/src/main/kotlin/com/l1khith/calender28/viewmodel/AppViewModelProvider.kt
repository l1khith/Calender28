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
                coinRepository = app.container.coinRepository
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
    }
}

/**
 * Extension function to retrieve [MatrixApplication] from [CreationExtras].
 */
fun CreationExtras.matrixApplication(): MatrixApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MatrixApplication)
