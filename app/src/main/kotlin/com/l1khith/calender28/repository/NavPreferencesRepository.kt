package com.l1khith.calender28.repository

import com.l1khith.calender28.data.BottomTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repository responsible for managing bottom navigation dock tab preferences and ordering.
 * Guarantees that at least one tab remains enabled at all times.
 */
interface NavPreferencesRepository {
    val enabledTabs: Flow<Set<BottomTab>>
    val tabOrder: Flow<List<BottomTab>>

    suspend fun toggleTab(tab: BottomTab, enabled: Boolean): Boolean
    suspend fun setTabs(tabs: Set<BottomTab>): Boolean
    suspend fun reorderTabs(fromIndex: Int, toIndex: Int)
    suspend fun setTabOrder(order: List<BottomTab>)
    suspend fun resetToDefaults()
}

class NavPreferencesRepositoryImpl(
    private val userPreferencesRepository: UserPreferencesRepository
) : NavPreferencesRepository {

    override val enabledTabs: Flow<Set<BottomTab>> =
        userPreferencesRepository.enabledBottomTabs.map { tabIds ->
            BottomTab.parseTabs(tabIds)
        }

    override val tabOrder: Flow<List<BottomTab>> =
        userPreferencesRepository.bottomTabOrder.map { orderStr ->
            BottomTab.parseOrder(orderStr)
        }

    override suspend fun toggleTab(tab: BottomTab, enabled: Boolean): Boolean {
        val currentIds = userPreferencesRepository.enabledBottomTabs.first()
        val currentTabs = BottomTab.parseTabs(currentIds).toMutableSet()

        return if (enabled) {
            currentTabs.add(tab)
            userPreferencesRepository.updateEnabledBottomTabs(BottomTab.toIds(currentTabs))
            true
        } else {
            // Cannot disable if only 1 tab remains
            if (currentTabs.size > 1 && currentTabs.contains(tab)) {
                currentTabs.remove(tab)
                userPreferencesRepository.updateEnabledBottomTabs(BottomTab.toIds(currentTabs))
                true
            } else {
                false
            }
        }
    }

    override suspend fun setTabs(tabs: Set<BottomTab>): Boolean {
        if (tabs.isEmpty()) return false
        userPreferencesRepository.updateEnabledBottomTabs(BottomTab.toIds(tabs))
        return true
    }

    override suspend fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val current = BottomTab.parseOrder(userPreferencesRepository.bottomTabOrder.first()).toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            userPreferencesRepository.updateBottomTabOrder(BottomTab.orderToString(current))
        }
    }

    override suspend fun setTabOrder(order: List<BottomTab>) {
        if (order.isEmpty()) return
        userPreferencesRepository.updateBottomTabOrder(BottomTab.orderToString(order))
    }

    override suspend fun resetToDefaults() {
        userPreferencesRepository.updateEnabledBottomTabs(BottomTab.DEFAULT_TAB_IDS)
        userPreferencesRepository.updateBottomTabOrder(BottomTab.DEFAULT_ORDER_STRING)
    }
}
