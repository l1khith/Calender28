package com.l1khith.calender28

import com.l1khith.calender28.data.BottomTab
import com.l1khith.calender28.repository.NavPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Fake implementation of [NavPreferencesRepository] for unit testing.
 */
class FakeNavPreferencesRepository : NavPreferencesRepository {
    private val tabsFlow = MutableStateFlow(BottomTab.DEFAULT_TABS)
    private val orderFlow = MutableStateFlow(BottomTab.ALL_TABS)

    override val enabledTabs: Flow<Set<BottomTab>> = tabsFlow
    override val tabOrder: Flow<List<BottomTab>> = orderFlow

    override suspend fun toggleTab(tab: BottomTab, enabled: Boolean): Boolean {
        val current = tabsFlow.value.toMutableSet()
        return if (enabled) {
            current.add(tab)
            tabsFlow.value = current
            true
        } else {
            if (current.size > 1 && current.contains(tab)) {
                current.remove(tab)
                tabsFlow.value = current
                true
            } else {
                false
            }
        }
    }

    override suspend fun setTabs(tabs: Set<BottomTab>): Boolean {
        if (tabs.isEmpty()) return false
        tabsFlow.value = tabs
        return true
    }

    override suspend fun reorderTabs(fromIndex: Int, toIndex: Int) {
        val current = orderFlow.value.toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            orderFlow.value = current
        }
    }

    override suspend fun setTabOrder(order: List<BottomTab>) {
        if (order.isEmpty()) return
        orderFlow.value = order
    }

    override suspend fun resetToDefaults() {
        tabsFlow.value = BottomTab.DEFAULT_TABS
        orderFlow.value = BottomTab.ALL_TABS
    }
}

class CustomizableNavTest {

    private lateinit var navRepo: FakeNavPreferencesRepository

    @Before
    fun setUp() {
        navRepo = FakeNavPreferencesRepository()
    }

    @Test
    fun bottomTab_canonicalOrderAndIndices_areCorrect() {
        assertEquals(4, BottomTab.ALL_TABS.size)
        assertEquals(BottomTab.MONTH, BottomTab.ALL_TABS[0])
        assertEquals(BottomTab.TASKS, BottomTab.ALL_TABS[1])
        assertEquals(BottomTab.HABIT, BottomTab.ALL_TABS[2])
        assertEquals(BottomTab.NOTES, BottomTab.ALL_TABS[3])

        assertEquals(0, BottomTab.MONTH.tabIndex)
        assertEquals(1, BottomTab.TASKS.tabIndex)
        assertEquals(2, BottomTab.HABIT.tabIndex)
        assertEquals(3, BottomTab.NOTES.tabIndex)
    }

    @Test
    fun bottomTab_parseTabs_handlesNullAndEmptyGracefully() {
        val fromNull = BottomTab.parseTabs(null)
        assertEquals(BottomTab.DEFAULT_TABS, fromNull)

        val fromEmpty = BottomTab.parseTabs(emptySet())
        assertEquals(BottomTab.DEFAULT_TABS, fromEmpty)

        val fromInvalid = BottomTab.parseTabs(setOf("invalid_tab", "unknown"))
        assertEquals(BottomTab.DEFAULT_TABS, fromInvalid)
    }

    @Test
    fun bottomTab_parseTabs_correctlyParsesValidIds() {
        val parsed = BottomTab.parseTabs(setOf("month", "notes"))
        assertEquals(setOf(BottomTab.MONTH, BottomTab.NOTES), parsed)

        // Case insensitivity
        val caseInsensitive = BottomTab.parseTabs(setOf("MONTH", "tasks"))
        assertEquals(setOf(BottomTab.MONTH, BottomTab.TASKS), caseInsensitive)
    }

    @Test
    fun bottomTab_parseOrder_preservesCustomSequence() {
        val customOrder = "notes,tasks,habit,month"
        val parsed = BottomTab.parseOrder(customOrder)
        assertEquals(
            listOf(BottomTab.NOTES, BottomTab.TASKS, BottomTab.HABIT, BottomTab.MONTH),
            parsed
        )
    }

    @Test
    fun bottomTab_parseOrder_appendsMissingCanonicalTabs() {
        // Only notes and tasks specified
        val partialOrder = "notes,tasks"
        val parsed = BottomTab.parseOrder(partialOrder)
        assertEquals(4, parsed.size)
        assertEquals(BottomTab.NOTES, parsed[0])
        assertEquals(BottomTab.TASKS, parsed[1])
        // Month and Habit should be automatically appended
        assertTrue(parsed.contains(BottomTab.MONTH))
        assertTrue(parsed.contains(BottomTab.HABIT))
    }

    @Test
    fun bottomTab_orderToString_producesExactCommaSeparatedString() {
        val order = listOf(BottomTab.TASKS, BottomTab.NOTES, BottomTab.MONTH, BottomTab.HABIT)
        val serialized = BottomTab.orderToString(order)
        assertEquals("tasks,notes,month,habit", serialized)
    }

    @Test
    fun navRepository_initialState_hasAllTabsEnabledAndCanonicalOrder() = runBlocking {
        val enabled = navRepo.enabledTabs.first()
        val order = navRepo.tabOrder.first()

        assertEquals(4, enabled.size)
        assertEquals(BottomTab.ALL_TABS, order)
    }

    @Test
    fun navRepository_reorderTabs_swapsAndShiftsCorrectly() = runBlocking {
        // Initial: Month (0), Tasks (1), Habit (2), Notes (3)
        // Move Notes (3) to front (0)
        navRepo.reorderTabs(fromIndex = 3, toIndex = 0)

        val newOrder = navRepo.tabOrder.first()
        assertEquals(BottomTab.NOTES, newOrder[0])
        assertEquals(BottomTab.MONTH, newOrder[1])
        assertEquals(BottomTab.TASKS, newOrder[2])
        assertEquals(BottomTab.HABIT, newOrder[3])
    }

    @Test
    fun navRepository_toggleTab_disablesTabWhenMultipleRemain() = runBlocking {
        val success = navRepo.toggleTab(BottomTab.NOTES, false)
        assertTrue(success)

        val enabled = navRepo.enabledTabs.first()
        assertEquals(3, enabled.size)
        assertFalse(enabled.contains(BottomTab.NOTES))
    }

    @Test
    fun navRepository_enforcesAtLeastOneTab_cannotDisableLastTab() = runBlocking {
        assertTrue(navRepo.toggleTab(BottomTab.TASKS, false))
        assertTrue(navRepo.toggleTab(BottomTab.HABIT, false))
        assertTrue(navRepo.toggleTab(BottomTab.NOTES, false))

        val remaining = navRepo.enabledTabs.first()
        assertEquals(1, remaining.size)
        assertTrue(remaining.contains(BottomTab.MONTH))

        // Attempt to disable the last tab -> must fail
        val failed = navRepo.toggleTab(BottomTab.MONTH, false)
        assertFalse(failed)
        assertEquals(1, navRepo.enabledTabs.first().size)
    }

    @Test
    fun navRepository_resetToDefaults_restoresBothOrderAndAllTabs() = runBlocking {
        navRepo.toggleTab(BottomTab.MONTH, false)
        navRepo.reorderTabs(2, 0)

        navRepo.resetToDefaults()

        assertEquals(BottomTab.DEFAULT_TABS, navRepo.enabledTabs.first())
        assertEquals(BottomTab.ALL_TABS, navRepo.tabOrder.first())
    }

    @Test
    fun visibleTabs_preservesCustomOrderFilteredByEnabledTabs() {
        val customOrder = listOf(BottomTab.NOTES, BottomTab.TASKS, BottomTab.HABIT, BottomTab.MONTH)
        val enabledTabs = setOf(BottomTab.MONTH, BottomTab.NOTES)

        val visibleTabs = customOrder.filter { enabledTabs.contains(it) }

        assertEquals(2, visibleTabs.size)
        assertEquals(BottomTab.NOTES, visibleTabs[0])
        assertEquals(BottomTab.MONTH, visibleTabs[1])
    }

    @Test
    fun fallbackRedirection_picksFirstVisibleTab_inCustomOrder() {
        val currentSelectedTab = 2 // Habit (which is being disabled)
        val customOrder = listOf(BottomTab.NOTES, BottomTab.TASKS, BottomTab.MONTH)
        val enabledTabs = setOf(BottomTab.NOTES, BottomTab.MONTH)

        val visibleTabs = customOrder.filter { enabledTabs.contains(it) }
        val validIndices = visibleTabs.map { it.tabIndex }

        val newSelectedTab = if (currentSelectedTab !in validIndices) {
            validIndices.first()
        } else {
            currentSelectedTab
        }

        // First visible tab in custom order is Notes (tabIndex 3)
        assertEquals(3, newSelectedTab)
    }
}
