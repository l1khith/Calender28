package com.l1khith.calender28.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_AVATAR_URL = stringPreferencesKey("user_avatar_url")
        val KEY_IS_PRO_USER = booleanPreferencesKey("is_pro_user")
        val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        val KEY_IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        val KEY_ENABLE_SPARKY = booleanPreferencesKey("enable_sparky")
        val KEY_ENABLE_ANIMATIONS = booleanPreferencesKey("enable_animations")
        val KEY_ENABLE_SOUNDS = booleanPreferencesKey("enable_sounds")
        val KEY_ENABLED_BOTTOM_TABS = stringSetPreferencesKey("enabled_bottom_tabs")
        val KEY_BOTTOM_TAB_ORDER = stringPreferencesKey("bottom_tab_order")

        // F1 Confidence Contract
        val KEY_BET_TIER = stringPreferencesKey("bet_tier")
        val KEY_BET_STREAK = intPreferencesKey("bet_streak")
        val KEY_BET_LOSS_STREAK = intPreferencesKey("bet_loss_streak")
        val KEY_BET_LAST_PLAYED_DATE = stringPreferencesKey("bet_last_played_date")
        val KEY_BET_SNAPSHOT_TASK_COUNT = intPreferencesKey("bet_snapshot_task_count")
        val KEY_BET_SNAPSHOT_TASK_IDS = stringSetPreferencesKey("bet_snapshot_task_ids")
        val KEY_BET_ACTIVE_TIER = stringPreferencesKey("bet_active_tier")

        // F2 Daily Task Notification
        val KEY_DAILY_REMINDER_HOUR = intPreferencesKey("daily_reminder_hour")
        val KEY_DAILY_REMINDER_MINUTE = intPreferencesKey("daily_reminder_minute")
        val KEY_DAILY_REMINDER_ENABLED = booleanPreferencesKey("daily_reminder_enabled")
        val KEY_DAILY_REMINDER_LAST_FIRED_DATE = stringPreferencesKey("daily_reminder_last_fired_date")

        // F4 Top Bar Slot Customization
        val KEY_TOP_BAR_SLOT_1 = stringPreferencesKey("top_bar_slot_1")

        const val DEFAULT_USER_NAME = "Guest"
        const val DEFAULT_USER_AVATAR_URL = ""
        const val DEFAULT_IS_PRO_USER = false
        const val DEFAULT_SELECTED_THEME = "DEFAULT"
        const val DEFAULT_IS_APP_LOCK_ENABLED = false
        const val DEFAULT_ENABLE_SPARKY = true
        const val DEFAULT_ENABLE_ANIMATIONS = true
        const val DEFAULT_ENABLE_SOUNDS = true
        val DEFAULT_ENABLED_BOTTOM_TABS = setOf("month", "tasks", "habit", "notes")
        const val DEFAULT_BOTTOM_TAB_ORDER = "month,tasks,habit,notes"

        const val DEFAULT_BET_TIER = "A"
        const val DEFAULT_DAILY_REMINDER_HOUR = 6
        const val DEFAULT_DAILY_REMINDER_MINUTE = 0
        const val DEFAULT_DAILY_REMINDER_ENABLED = true
        const val DEFAULT_TOP_BAR_SLOT_1 = "matrix28"
        const val DEFAULT_BET_STREAK = 0
        const val DEFAULT_BET_LOSS_STREAK = 0

        @Volatile
        private var instance: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository =
            instance ?: synchronized(this) {
                instance ?: UserPreferencesRepository(createDataStore(context.applicationContext)).also {
                    instance = it
                }
            }
    }

    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: DEFAULT_USER_NAME
    }

    val userAvatarUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_USER_AVATAR_URL] ?: DEFAULT_USER_AVATAR_URL
    }

    val isProUser: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_PRO_USER] ?: DEFAULT_IS_PRO_USER
    }

    val selectedTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_THEME] ?: DEFAULT_SELECTED_THEME
    }

    val isAppLockEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_IS_APP_LOCK_ENABLED] ?: DEFAULT_IS_APP_LOCK_ENABLED
    }

    val enableSparky: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ENABLE_SPARKY] ?: DEFAULT_ENABLE_SPARKY
    }

    val enableAnimations: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ENABLE_ANIMATIONS] ?: DEFAULT_ENABLE_ANIMATIONS
    }

    val enableSounds: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_ENABLE_SOUNDS] ?: DEFAULT_ENABLE_SOUNDS
    }

    val enabledBottomTabs: Flow<Set<String>> = dataStore.data.map { preferences ->
        val saved = preferences[KEY_ENABLED_BOTTOM_TABS]
        if (saved.isNullOrEmpty()) DEFAULT_ENABLED_BOTTOM_TABS else saved
    }

    val bottomTabOrder: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BOTTOM_TAB_ORDER] ?: DEFAULT_BOTTOM_TAB_ORDER
    }

    val optionalUserName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME]?.trim()?.ifEmpty { null }
    }

    val betTier: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BET_TIER] ?: DEFAULT_BET_TIER
    }

    val betStreak: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_BET_STREAK] ?: DEFAULT_BET_STREAK
    }

    val betLossStreak: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_BET_LOSS_STREAK] ?: DEFAULT_BET_LOSS_STREAK
    }

    val betLastPlayedDate: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_BET_LAST_PLAYED_DATE]
    }

    val betSnapshotTaskCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_BET_SNAPSHOT_TASK_COUNT] ?: 0
    }

    val betSnapshotTaskIds: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_BET_SNAPSHOT_TASK_IDS] ?: emptySet()
    }

    val betActiveTier: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_BET_ACTIVE_TIER]
    }

    val dailyReminderHour: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_HOUR] ?: DEFAULT_DAILY_REMINDER_HOUR
    }

    val dailyReminderMinute: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_MINUTE] ?: DEFAULT_DAILY_REMINDER_MINUTE
    }

    val dailyReminderEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_ENABLED] ?: DEFAULT_DAILY_REMINDER_ENABLED
    }

    val dailyReminderLastFiredDate: Flow<String?> = dataStore.data.map { preferences ->
        preferences[KEY_DAILY_REMINDER_LAST_FIRED_DATE]
    }

    val topBarSlot1: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_TOP_BAR_SLOT_1] ?: DEFAULT_TOP_BAR_SLOT_1
    }

    suspend fun updateUserName(name: String?) {
        dataStore.edit { preferences ->
            if (name == null) {
                preferences.remove(KEY_USER_NAME)
            } else {
                preferences[KEY_USER_NAME] = name.trim()
            }
        }
    }

    suspend fun updateBetTier(tier: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BET_TIER] = tier
        }
    }

    suspend fun updateBetStreak(streak: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_BET_STREAK] = streak
        }
    }

    suspend fun updateBetLossStreak(lossStreak: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_BET_LOSS_STREAK] = lossStreak
        }
    }

    suspend fun updateBetLastPlayedDate(dateStr: String?) {
        dataStore.edit { preferences ->
            if (dateStr == null) {
                preferences.remove(KEY_BET_LAST_PLAYED_DATE)
            } else {
                preferences[KEY_BET_LAST_PLAYED_DATE] = dateStr
            }
        }
    }

    suspend fun recordBetSnapshot(dateStr: String, tier: String, taskCount: Int, taskIds: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_BET_LAST_PLAYED_DATE] = dateStr
            preferences[KEY_BET_ACTIVE_TIER] = tier
            preferences[KEY_BET_SNAPSHOT_TASK_COUNT] = taskCount
            preferences[KEY_BET_SNAPSHOT_TASK_IDS] = taskIds
        }
    }

    suspend fun clearActiveBet() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_BET_ACTIVE_TIER)
            preferences.remove(KEY_BET_SNAPSHOT_TASK_COUNT)
            preferences.remove(KEY_BET_SNAPSHOT_TASK_IDS)
        }
    }

    suspend fun updateDailyReminderTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_HOUR] = hour
            preferences[KEY_DAILY_REMINDER_MINUTE] = minute
        }
    }

    suspend fun updateDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun updateDailyReminderLastFiredDate(dateStr: String) {
        dataStore.edit { preferences ->
            preferences[KEY_DAILY_REMINDER_LAST_FIRED_DATE] = dateStr
        }
    }

    suspend fun updateTopBarSlot1(slot: String) {
        dataStore.edit { preferences ->
            preferences[KEY_TOP_BAR_SLOT_1] = slot
        }
    }

    suspend fun updateUserAvatarUrl(avatarUrl: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_AVATAR_URL] = avatarUrl
        }
    }

    suspend fun updateIsProUser(isPro: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_PRO_USER] = isPro
        }
    }

    suspend fun updateSelectedTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_THEME] = theme
        }
    }

    suspend fun updateIsAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_IS_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun updateEnableSparky(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLE_SPARKY] = enabled
        }
    }

    suspend fun updateEnableAnimations(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLE_ANIMATIONS] = enabled
        }
    }

    suspend fun updateEnableSounds(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLE_SOUNDS] = enabled
        }
    }

    suspend fun updateEnabledBottomTabs(tabs: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_ENABLED_BOTTOM_TABS] = tabs
        }
    }

    suspend fun updateBottomTabOrder(order: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BOTTOM_TAB_ORDER] = order
        }
    }
}

