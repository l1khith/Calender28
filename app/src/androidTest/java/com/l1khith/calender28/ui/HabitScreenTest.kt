package com.l1khith.calender28.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.test.TestDataFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun habitSection_displaysHeader() {
        composeTestRule.setContent {
            HabitSection(
                viewModel = null,
                isProActive = true
            )
        }

        composeTestRule.onNodeWithText("28-Day Habit Cycles").assertIsDisplayed()
        composeTestRule.onNodeWithText("Build streaks in perfect 4-week blocks").assertIsDisplayed()
    }

    @Test
    fun habitSection_displaysEmptyStateWhenNoHabits() {
        composeTestRule.setContent {
            HabitSection(
                viewModel = null,
                isProActive = true
            )
        }

        composeTestRule.onNodeWithText("No Active Habit Cycles").assertIsDisplayed()
        composeTestRule.onNodeWithText("+ Create New Habit Cycle").assertIsDisplayed()
    }

    @Test
    fun habitDetailScreen_displaysHabitTitleAndGrid() {
        val habit = TestDataFactory.createHabit(name = "Meditation Habit", completedDays = setOf(1))

        composeTestRule.setContent {
            HabitDetailScreen(
                habit = habit,
                onBack = {},
                onToggleDay = {},
                onUpdateHabit = {},
                onDeleteHabit = {}
            )
        }

        composeTestRule.onNodeWithText("Meditation Habit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cycle Pattern").assertIsDisplayed()
    }
}
