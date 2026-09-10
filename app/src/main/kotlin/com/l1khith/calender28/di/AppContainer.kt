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
        TaskRepositoryImpl(appContext)
    }

    override val habitRepository: HabitRepository by lazy {
        HabitRepositoryImpl(appContext)
    }

    override val coinRepository: CoinRepository by lazy {
        CoinRepositoryImpl(
            coinDao = database.coinDao(),
            userPrefsRepo = userPreferencesRepository
        )
    }

    override val focusRepository: FocusRepository by lazy {
        FocusRepositoryImpl(appContext)
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
}
