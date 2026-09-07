package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.*
import com.l1khith.calender28.repository.SparkyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class SparkyUiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : SparkyUiEvent()
    data class AchievementUnlocked(val title: String, val reward: Int) : SparkyUiEvent()
}

class SparkyViewModel(
    application: Application,
    private val sparkyRepository: SparkyRepository
) : AndroidViewModel(application) {

    val sparkyState: StateFlow<SparkyState> = sparkyRepository.sparkyState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SparkyState()
        )

    private val _currentMood = MutableStateFlow(SparkyMood.IDLE)
    val currentMood: StateFlow<SparkyMood> = _currentMood.asStateFlow()

    private val _moodMessage = MutableStateFlow("Ready for today's achievements!")
    val moodMessage: StateFlow<String> = _moodMessage.asStateFlow()

    private val _evolutionEvent = MutableSharedFlow<EvolutionStage>()
    val evolutionEvent: SharedFlow<EvolutionStage> = _evolutionEvent.asSharedFlow()

    private val _uiEvent = MutableSharedFlow<SparkyUiEvent>()
    val uiEvent: SharedFlow<SparkyUiEvent> = _uiEvent.asSharedFlow()

    private var moodResetJob: Job? = null

    private val interactionPhrases = listOf(
        "I'm always cheering for your daily wins!",
        "Every checked habit builds our 28-day matrix!",
        "Look at that consistency! Keep the spark alive!",
        "Focused and ready! What's next on our agenda?",
        "Proud to be your matrix companion today!",
        "Consistency is our secret superpower!"
    )
    private var phraseIndex = 0

    fun triggerHabitComplete(habitName: String = "") {
        viewModelScope.launch {
            val result = sparkyRepository.onHabitCompleted()
            if (result.didEvolve) {
                setTemporaryMood(SparkyMood.EVOLVING, "Sparky is evolving into ${result.newStage.displayName}!", 6000)
                _evolutionEvent.emit(result.newStage)
            } else if (result.didLevelUp) {
                setTemporaryMood(SparkyMood.CELEBRATING, "Level Up! Sparky is now Level ${result.newState.level}!", 4000)
            } else {
                val msg = if (habitName.isNotBlank()) "Great job completing $habitName!" else "Awesome habit progress!"
                setTemporaryMood(SparkyMood.HAPPY, msg, 3500)
            }
        }
    }

    fun triggerTaskComplete(taskTitle: String = "") {
        viewModelScope.launch {
            val result = sparkyRepository.onTaskCompleted()
            if (result.didLevelUp) {
                setTemporaryMood(SparkyMood.CELEBRATING, "Level Up! Sparky reached Level ${result.newState.level}!", 4000)
            } else {
                val msg = if (taskTitle.isNotBlank()) "Checked off '$taskTitle'!" else "Task crushed!"
                setTemporaryMood(SparkyMood.PROUD, msg, 3000)
            }
        }
    }

    fun triggerFocusComplete(durationMinutes: Int) {
        viewModelScope.launch {
            val result = sparkyRepository.onFocusCompleted(durationMinutes)
            setTemporaryMood(SparkyMood.CALM, "Deep focus completed! Sparky feels calm and energized.", 4000)
        }
    }

    fun triggerStreakUpdate(streak: Int) {
        viewModelScope.launch {
            sparkyRepository.onStreakUpdated(streak)
            if (streak >= 3) {
                setTemporaryMood(SparkyMood.CELEBRATING, "$streak-day streak! The fire burns bright!", 3500)
            }
        }
    }

    fun interactWithSparky() {
        val phrase = interactionPhrases[phraseIndex % interactionPhrases.size]
        phraseIndex++
        setTemporaryMood(SparkyMood.HAPPY, phrase, 3000)
    }

    private fun setTemporaryMood(mood: SparkyMood, message: String, durationMs: Long) {
        moodResetJob?.cancel()
        _currentMood.value = mood
        _moodMessage.value = message
        moodResetJob = viewModelScope.launch {
            delay(durationMs)
            _currentMood.value = SparkyMood.IDLE
            _moodMessage.value = "Resting peacefully."
        }
    }

    fun buyShopItem(item: SparkyShopItem) {
        viewModelScope.launch {
            val result = sparkyRepository.buyShopItem(item)
            result.onSuccess {
                _uiEvent.emit(SparkyUiEvent.ShowMessage("Unlocked ${item.name} for Sparky!"))
                setTemporaryMood(SparkyMood.CELEBRATING, "Sparky loves the new ${item.name}!", 4000)
            }.onFailure { err ->
                _uiEvent.emit(SparkyUiEvent.ShowMessage(err.message ?: "Could not purchase item", isError = true))
            }
        }
    }

    fun toggleEquip(item: SparkyShopItem) {
        viewModelScope.launch {
            val result = sparkyRepository.toggleEquip(item)
            result.onSuccess {
                val isEquipped = when (item.category) {
                    SparkyShopCategory.HATS -> sparkyState.value.equippedHat == item.id
                    SparkyShopCategory.SKINS -> sparkyState.value.equippedSkin == item.id
                    else -> false
                }
                val msg = if (isEquipped) "Unequipped ${item.name}" else "Equipped ${item.name} on Sparky"
                _uiEvent.emit(SparkyUiEvent.ShowMessage(msg))
            }.onFailure { err ->
                _uiEvent.emit(SparkyUiEvent.ShowMessage(err.message ?: "Could not equip item", isError = true))
            }
        }
    }

    fun renameSparky(name: String) {
        viewModelScope.launch {
            val result = sparkyRepository.renameSparky(name)
            result.onSuccess {
                _uiEvent.emit(SparkyUiEvent.ShowMessage("Renamed companion to '$name'"))
            }.onFailure { err ->
                _uiEvent.emit(SparkyUiEvent.ShowMessage(err.message ?: "Could not rename", isError = true))
            }
        }
    }

    fun claimAchievement(achievementId: String) {
        viewModelScope.launch {
            val result = sparkyRepository.claimAchievement(achievementId)
            result.onSuccess { coins ->
                _uiEvent.emit(SparkyUiEvent.ShowMessage("Claimed +$coins CalCoins reward!"))
            }.onFailure { err ->
                _uiEvent.emit(SparkyUiEvent.ShowMessage(err.message ?: "Could not claim", isError = true))
            }
        }
    }

    fun getAchievements(): List<SparkyAchievement> {
        return sparkyRepository.getAchievements(sparkyState.value)
    }
}
