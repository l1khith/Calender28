package com.l1khith.calender28.utils

import android.content.Context
import com.l1khith.calender28.data.BottomTab
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

    private val _enabledTabs = MutableStateFlow<Set<BottomTab>>(BottomTab.DEFAULT_TABS)
    val enabledTabs: StateFlow<Set<BottomTab>> = _enabledTabs.asStateFlow()

    private val _tabOrder = MutableStateFlow<List<BottomTab>>(BottomTab.ALL_TABS)
    val tabOrder: StateFlow<List<BottomTab>> = _tabOrder.asStateFlow()

    private val _topBarSlot1 = MutableStateFlow(com.l1khith.calender28.domain.model.TopBarSlotContent.MATRIX28)
    val topBarSlot1: StateFlow<com.l1khith.calender28.domain.model.TopBarSlotContent> = _topBarSlot1.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    val userName: StateFlow<String?> = _userName.asStateFlow()

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
            launch { repo.enableSparky.collect { _enableSparky.value = it } }
            launch { repo.enableAnimations.collect { _enableAnimations.value = it } }
            launch { repo.enableSounds.collect { _enableSounds.value = it } }
            launch {
                repo.enabledBottomTabs.collect { ids ->
                    _enabledTabs.value = BottomTab.parseTabs(ids)
                }
            }
            launch {
                repo.bottomTabOrder.collect { orderStr ->
                    _tabOrder.value = BottomTab.parseOrder(orderStr)
                }
            }
            launch {
                repo.topBarSlot1.collect { slotStr ->
                    _topBarSlot1.value = com.l1khith.calender28.domain.model.TopBarSlotContent.fromId(slotStr)
                }
            }
            launch {
                repo.optionalUserName.collect { name ->
                    _userName.value = name
                }
            }
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

    fun toggleTab(tab: BottomTab, enabled: Boolean): Boolean {
        val current = _enabledTabs.value.toMutableSet()
        if (enabled) {
            current.add(tab)
        } else {
            if (current.size <= 1 || !current.contains(tab)) return false
            current.remove(tab)
        }
        _enabledTabs.value = current
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateEnabledBottomTabs(BottomTab.toIds(current))
            } catch (_: Exception) {}
        }
        return true
    }

    fun setTabOrder(order: List<BottomTab>) {
        if (order.isEmpty()) return
        _tabOrder.value = order
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateBottomTabOrder(BottomTab.orderToString(order))
            } catch (_: Exception) {}
        }
    }

    fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val current = _tabOrder.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            _tabOrder.value = current
            scope?.launch(Dispatchers.IO) {
                try {
                    prefsRepo?.updateBottomTabOrder(BottomTab.orderToString(current))
                } catch (_: Exception) {}
            }
        }
    }

    fun resetTabs() {
        val defaultTabs = BottomTab.DEFAULT_TABS
        val defaultOrder = BottomTab.ALL_TABS
        _enabledTabs.value = defaultTabs
        _tabOrder.value = defaultOrder
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateEnabledBottomTabs(BottomTab.DEFAULT_TAB_IDS)
                prefsRepo?.updateBottomTabOrder(BottomTab.DEFAULT_ORDER_STRING)
            } catch (_: Exception) {}
        }
    }

    fun setTopBarSlot1(slot: com.l1khith.calender28.domain.model.TopBarSlotContent) {
        _topBarSlot1.value = slot
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateTopBarSlot1(slot.id)
            } catch (_: Exception) {}
        }
    }

    fun setUserName(name: String?) {
        val clean = name?.trim()?.take(20)
        val finalVal = if (clean.isNullOrEmpty()) null else clean
        _userName.value = finalVal
        scope?.launch(Dispatchers.IO) {
            try {
                prefsRepo?.updateUserName(finalVal)
            } catch (_: Exception) {}
        }
    }
}
