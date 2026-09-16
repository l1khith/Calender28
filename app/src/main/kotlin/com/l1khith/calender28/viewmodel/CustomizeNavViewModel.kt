package com.l1khith.calender28.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.l1khith.calender28.data.BottomTab
import com.l1khith.calender28.repository.NavPreferencesRepository
import com.l1khith.calender28.utils.AppSettingsManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomizeNavViewModel(
    application: Application,
    private val navRepository: NavPreferencesRepository
) : AndroidViewModel(application) {

    private val _enabledTabs = MutableStateFlow<Set<BottomTab>>(BottomTab.DEFAULT_TABS)
    val enabledTabs: StateFlow<Set<BottomTab>> = _enabledTabs.asStateFlow()

    private val _tabOrder = MutableStateFlow<List<BottomTab>>(BottomTab.ALL_TABS)
    val tabOrder: StateFlow<List<BottomTab>> = _tabOrder.asStateFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent: SharedFlow<String> = _errorEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            navRepository.enabledTabs.collect { tabs ->
                _enabledTabs.value = tabs
            }
        }
        viewModelScope.launch {
            navRepository.tabOrder.collect { order ->
                _tabOrder.value = order
            }
        }
    }

    /**
     * Checks whether a specific tab is currently enabled.
     */
    fun isTabEnabled(tab: BottomTab): Boolean = _enabledTabs.value.contains(tab)

    /**
     * Checks if this tab is the last remaining enabled tab and therefore locked from being disabled.
     */
    fun isTabLocked(tab: BottomTab): Boolean =
        _enabledTabs.value.size <= 1 && _enabledTabs.value.contains(tab)

    /**
     * Toggles the enabled state of [tab].
     * Fails if attempting to disable the last remaining tab.
     */
    fun toggleTab(tab: BottomTab, enabled: Boolean) {
        if (!enabled && isTabLocked(tab)) {
            viewModelScope.launch {
                _errorEvent.emit("At least one tab must remain enabled")
            }
            return
        }

        viewModelScope.launch {
            val success = navRepository.toggleTab(tab, enabled)
            if (success) {
                AppSettingsManager.toggleTab(tab, enabled)
            } else {
                _errorEvent.emit("At least one tab must remain enabled")
            }
        }
    }

    /**
     * Reorders a tab from [fromIndex] to [toIndex].
     * Applies the change immediately to in-memory state and persists to DataStore.
     */
    fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val current = _tabOrder.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _tabOrder.value = current
            AppSettingsManager.setTabOrder(current)
            viewModelScope.launch {
                navRepository.setTabOrder(current)
            }
        }
    }

    /**
     * Moves a tab one position up in the list.
     */
    fun moveUp(tab: BottomTab) {
        val index = _tabOrder.value.indexOf(tab)
        if (index > 0) {
            reorderTabs(index, index - 1)
        }
    }

    /**
     * Moves a tab one position down in the list.
     */
    fun moveDown(tab: BottomTab) {
        val index = _tabOrder.value.indexOf(tab)
        if (index >= 0 && index < _tabOrder.value.lastIndex) {
            reorderTabs(index, index + 1)
        }
    }

    /**
     * Resets navigation tabs to the default set (all 4 tabs enabled in canonical order)
     * and resets top bar title slot to default (Matrix 28).
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            navRepository.resetToDefaults()
            AppSettingsManager.resetTabs()
            AppSettingsManager.setTopBarSlot1(com.l1khith.calender28.domain.model.TopBarSlotContent.MATRIX28)
        }
    }
}
