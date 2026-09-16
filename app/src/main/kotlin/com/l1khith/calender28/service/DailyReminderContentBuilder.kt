package com.l1khith.calender28.service

import com.l1khith.calender28.domain.model.BetStatus

data class DailyReminderNotificationContent(
    val title: String,
    val message: String,
    val openBetSheet: Boolean
)

object DailyReminderContentBuilder {

    fun build(
        userName: String?,
        taskCount: Int,
        firstTaskTitle: String? = null,
        betStatus: BetStatus? = null,
        isRecoveryDay: Boolean = false
    ): DailyReminderNotificationContent {
        val cleanName = userName?.trim()
        val title = if (!cleanName.isNullOrEmpty()) {
            "Good morning, $cleanName!"
        } else {
            "Morning Briefing"
        }

        if (taskCount == 0) {
            val message = if (isRecoveryDay) {
                "No tasks scheduled today. Enjoy your recovery day!"
            } else {
                "No tasks scheduled for today. Plan ahead or take a breather!"
            }
            return DailyReminderNotificationContent(
                title = title,
                message = message,
                openBetSheet = false
            )
        }

        val taskPart = when (taskCount) {
            1 -> {
                if (!firstTaskTitle.isNullOrBlank()) {
                    "You have 1 task today: $firstTaskTitle."
                } else {
                    "You have 1 task scheduled for today."
                }
            }
            else -> "You have $taskCount tasks scheduled for today."
        }

        val betPart: String?
        var openBet = false

        when {
            isRecoveryDay -> {
                betPart = "Recovery Day: Take it easy, no bets today."
            }
            betStatus is BetStatus.Active -> {
                betPart = "Confidence bet active: ${betStatus.tier.name} tier (${betStatus.requiredToWin} to win)."
            }
            betStatus is BetStatus.ReadyToBet -> {
                betPart = "Place your confidence bet before 12:00 PM!"
                openBet = true
            }
            taskCount >= 3 && betStatus == null -> {
                betPart = "Place your confidence bet before 12:00 PM!"
                openBet = true
            }
            else -> {
                betPart = null
            }
        }

        val fullMessage = if (betPart != null) {
            "$taskPart $betPart"
        } else {
            taskPart
        }

        return DailyReminderNotificationContent(
            title = title,
            message = fullMessage,
            openBetSheet = openBet
        )
    }
}
