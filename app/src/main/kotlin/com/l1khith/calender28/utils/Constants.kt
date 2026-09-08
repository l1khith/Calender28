package com.l1khith.calender28.utils

/**
 * Production-ready centralized constants for Calender28.
 * Contains all reward economy amounts, AdMob IDs, limits, intervals, and URLs.
 */
object Constants {

    // --- App Metadata ---
    const val APP_NAME = "Calender28"
    const val APP_TAGLINE = "Fixed 28-Day Calendar & Matrix Productivity"
    const val DEVELOPER_EMAIL = "likid2d21@gmail.com"
    const val GITHUB_REPO_URL = "https://github.com/l1khith/Calender28"
    const val PRIVACY_POLICY_URL = "https://l1khith.github.io/Calender28"
    const val TERMS_OF_SERVICE_URL = "https://l1khith.github.io/Calender28"

    // --- Calendar & Habit Engine ---
    const val DAYS_PER_CYCLE = 28
    const val MONTHS_PER_YEAR = 13
    const val DAYS_PER_WEEK = 7
    const val FREE_TIER_MAX_HABITS = 5

    // --- CalCoin Rewards Economy ---
    const val REWARD_DAILY_LOGIN = 1
    const val REWARD_HABIT_DAY_LOG = 1
    const val REWARD_RECURRING_TASK = 2
    const val REWARD_HABIT_CYCLE_COMPLETE = 10
    const val REWARD_TASK_COMPLETE = 1
    const val REWARD_FOCUS_SESSION = 5

    // Streak Bonuses
    const val REWARD_STREAK_7_DAY = 50
    const val REWARD_STREAK_14_DAY = 100
    const val REWARD_STREAK_30_DAY = 200
    const val REWARD_STREAK_1000_DAY = 1000

    // Habit Cycle Milestones
    const val REWARD_MILESTONE_10_CYCLES = 100
    const val REWARD_MILESTONE_50_CYCLES = 500
    const val REWARD_MILESTONE_100_CYCLES = 1000

    // Premium Unlock Pricing
    const val PREMIUM_UNLOCK_COIN_COST = 1500

    // Promo Codes
    const val PROMO_CODE_SHIPATON = "SHIPATON2026"
    const val PROMO_REWARD_SHIPATON = 100

    const val PROMO_CODE_WELCOME = "WELCOME10"
    const val PROMO_REWARD_WELCOME = 10

    const val PROMO_CODE_CALENDER28 = "CALENDER28"
    const val PROMO_REWARD_CALENDER28 = 50

    const val PROMO_CODE_MATRIXPRO = "MATRIXPRO"
    const val PROMO_REWARD_MATRIXPRO = 1500

    // --- AdMob Configuration ---
    const val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_ADMOB_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_ADMOB_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"

    // Interstitial Ad Frequency
    const val INTERSTITIAL_NAV_FREQUENCY = 5
    const val INTERSTITIAL_COUNTDOWN_SECONDS = 5

    // --- Database Configuration ---
    const val DATABASE_NAME = "task_database"
    const val DATABASE_VERSION = 4

    // --- Notification Channels ---
    const val CHANNEL_TASK_REMINDERS = "task_reminders"
    const val CHANNEL_RECURRING_REMINDERS = "recurring_reminders"
    const val CHANNEL_HABIT_REMINDERS = "habit_reminders"
    const val CHANNEL_FOCUS_MODE = "focus_mode_channel"
    const val CHANNEL_SYSTEM = "system"

    // --- Focus Mode Defaults ---
    const val DEFAULT_POMODORO_WORK_MINUTES = 25
    const val DEFAULT_POMODORO_SHORT_BREAK_MINUTES = 5
    const val DEFAULT_POMODORO_LONG_BREAK_MINUTES = 15
}
