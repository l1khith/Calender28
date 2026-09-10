package com.l1khith.calender28.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents a manageable bottom navigation tab in Calender28.
 *
 * Fixed canonical order is:
 * 0: MONTH
 * 1: TASKS
 * 2: HABIT
 * 3: NOTES
 */
enum class BottomTab(
    val id: String,
    val tabIndex: Int,
    val label: String,
    val description: String,
    val icon: ImageVector
) {
    MONTH(
        id = "month",
        tabIndex = 0,
        label = "Month",
        description = "Calendar matrix, events, and monthly overview",
        icon = Icons.Default.CalendarMonth
    ),
    TASKS(
        id = "tasks",
        tabIndex = 1,
        label = "Tasks",
        description = "Daily tasks, checklists, and agenda items",
        icon = Icons.Default.Checklist
    ),
    HABIT(
        id = "habit",
        tabIndex = 2,
        label = "Habit",
        description = "28-day habit cycles, streaks, and tracking",
        icon = Icons.Default.LocalFireDepartment
    ),
    NOTES(
        id = "notes",
        tabIndex = 3,
        label = "Notes",
        description = "Personal notes and markdown documentation",
        icon = Icons.Default.EditNote
    );

    companion object {
        val ALL_TABS: List<BottomTab> = entries.sortedBy { it.tabIndex }
        val DEFAULT_TABS: Set<BottomTab> = entries.toSet()
        val DEFAULT_TAB_IDS: Set<String> = entries.map { it.id }.toSet()

        fun fromId(id: String): BottomTab? = entries.find { it.id.equals(id, ignoreCase = true) }

        fun fromTabIndex(index: Int): BottomTab? = entries.find { it.tabIndex == index }

        /**
         * Safely parses a set of tab IDs into canonical [BottomTab]s.
         * If the provided set is null, empty, or contains only invalid IDs,
         * gracefully falls back to [DEFAULT_TABS].
         */
        fun parseTabs(ids: Set<String>?): Set<BottomTab> {
            if (ids.isNullOrEmpty()) return DEFAULT_TABS
            val matched = ids.mapNotNull { fromId(it) }.toSet()
            return if (matched.isEmpty()) DEFAULT_TABS else matched
        }

        const val DEFAULT_ORDER_STRING = "month,tasks,habit,notes"

        fun toIds(tabs: Set<BottomTab>): Set<String> = tabs.map { it.id }.toSet()

        /**
         * Parses a comma-separated tab order string into a distinct [List] of [BottomTab]s.
         * Ensures that all canonical tabs are included (missing tabs are appended in canonical order).
         */
        fun parseOrder(orderStr: String?): List<BottomTab> {
            if (orderStr.isNullOrBlank()) return ALL_TABS
            val parsed = orderStr.split(",")
                .map { it.trim() }
                .mapNotNull { fromId(it) }
                .distinct()
                .toMutableList()

            // Append any missing canonical tabs
            for (tab in ALL_TABS) {
                if (tab !in parsed) {
                    parsed.add(tab)
                }
            }
            return parsed
        }

        fun orderToString(tabs: List<BottomTab>): String =
            tabs.joinToString(",") { it.id }
    }
}
