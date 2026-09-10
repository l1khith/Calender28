package com.l1khith.calender28.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
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

    suspend fun updateUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
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

