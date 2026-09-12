package com.l1khith.calender28.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.l1khith.calender28.data.AppTask
import com.l1khith.calender28.data.Habit
import com.l1khith.calender28.test.TestDataFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.l1khith.calender28.viewmodel.FixedCalendarViewModel

@RunWith(AndroidJUnit4::class)
class TasksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val viewModel by lazy { FixedCalendarViewModel(application) }

    @Test
    fun tasksScreen_displaysHeaderTitles() {
        composeTestRule.setContent {
            TasksScreen(
                viewModel = viewModel,
                onEditTask = {},
                isProActive = true
            )
        }

        composeTestRule.onNodeWithText("Urgent & Overdue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today's Focus").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upcoming Tasks").assertIsDisplayed()
    }

    @Test
    fun tasksScreen_displaysEmptyStatesWhenNoTasks() {
        composeTestRule.setContent {
            TasksScreen(
                viewModel = viewModel,
                onEditTask = {},
                isProActive = true
            )
        }

        composeTestRule.onNodeWithText("No urgent or overdue tasks.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No tasks focus for today.").assertIsDisplayed()
        composeTestRule.onNodeWithText("No upcoming tasks.").assertIsDisplayed()
    }

    @Test
    fun urgentTaskCard_displaysTaskTitle() {
        val task = TestDataFactory.createAppTask(id = "u-1", title = "Urgent Doctor Visit", associatedDate = "2026-08-01")

        composeTestRule.setContent {
            UrgentTaskCard(
                task = task,
                todayDateStr = "2026-08-14",
                onToggleComplete = {},
                onClick = {},
                onDeleteTask = {}
            )
        }

        composeTestRule.onNodeWithText("Urgent Doctor Visit").assertIsDisplayed()
    }

    @Test
    fun focusTaskCard_displaysTaskTitle() {
        val task = TestDataFactory.createAppTask(id = "f-1", title = "Focus Presentation", associatedDate = "2026-08-14")

        composeTestRule.setContent {
            FocusTaskCard(
                task = task,
                onToggleComplete = {},
                onClick = {},
                onDeleteTask = {}
            )
        }

        composeTestRule.onNodeWithText("Focus Presentation").assertIsDisplayed()
    }

    @Test
    fun upcomingTaskCard_displaysTaskTitle() {
        val task = TestDataFactory.createAppTask(id = "up-1", title = "Future Project Launch", associatedDate = "2026-08-20")

        composeTestRule.setContent {
            UpcomingTaskCard(
                task = task,
                onToggleComplete = {},
                onClick = {},
                onDeleteTask = {}
            )
        }

        composeTestRule.onNodeWithText("Future Project Launch").assertIsDisplayed()
    }
}
