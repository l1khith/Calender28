package com.l1khith.calender28.utils

import android.content.Context
import com.l1khith.calender28.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AppSettingsManager {

    private val _enableSparky = MutableStateFlow(true)
    val enableSparky: StateFlow<Boolean> = _enableSparky.asStateFlow()

    private val _enableAnimations = MutableStateFlow(true)
    val enableAnimations: StateFlow<Boolean> = _enableAnimations.asStateFlow()

    private val _enableSounds = MutableStateFlow(true)
    val enableSounds: StateFlow<Boolean> = _enableSounds.asStateFlow()

    private var prefsRepo: UserPreferencesRepository? = null
    private var scope: CoroutineScope? = null
    private var isInitialized = false

    fun init(context: Context, coroutineScope: CoroutineScope) {
        if (isInitialized) return
        isInitialized = true
        val repo = UserPreferencesRepository.getInstance(context.applicationContext)
        prefsRepo = repo
        scope = coroutineScope

        coroutineScope.launch(Dispatchers.Default) {
            repo.enableSparky.collect { _enableSparky.value = it }
        }
        coroutineScope.launch(Dispatchers.Default) {
            repo.enableAnimations.collect { _enableAnimations.value = it }
        }
        coroutineScope.launch(Dispatchers.Default) {
            repo.enableSounds.collect { _enableSounds.value = it }
        }
    }

    fun setEnableSparky(enabled: Boolean) {
        _enableSparky.value = enabled
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateEnableSparky(enabled)
            } catch (_: Exception) {}
        }
    }

    fun setEnableAnimations(enabled: Boolean) {
        _enableAnimations.value = enabled
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateEnableAnimations(enabled)
            } catch (_: Exception) {}
        }
    }

    fun setEnableSounds(enabled: Boolean) {
        _enableSounds.value = enabled
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateEnableSounds(enabled)
            } catch (_: Exception) {}
        }
    }
}
