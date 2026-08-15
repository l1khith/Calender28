package com.l1khith.calender28.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

        const val DEFAULT_USER_NAME = "Guest"
        const val DEFAULT_USER_AVATAR_URL = ""
        const val DEFAULT_IS_PRO_USER = false
        const val DEFAULT_SELECTED_THEME = "DEFAULT"
        const val DEFAULT_IS_APP_LOCK_ENABLED = false
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
}

