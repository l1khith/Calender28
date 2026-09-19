package com.l1khith.calender28.data.user

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.userIdDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_id_store"
)

object UserIdManager {

    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val DISPLAY_NAME_KEY = stringPreferencesKey("display_name")
    private val IS_CUSTOM_NAME_KEY = booleanPreferencesKey("is_custom_name")
    private val CREATION_TIMESTAMP_KEY = longPreferencesKey("creation_timestamp")

    // ============ USER ID ============

    suspend fun getOrCreate(context: Context): String {
        val store = context.applicationContext.userIdDataStore
        val existing = store.data.map { it[USER_ID_KEY] }.first()
        if (existing != null) return existing

        val newId = UUID.randomUUID().toString()
        store.edit { it[USER_ID_KEY] = newId }
        return newId
    }

    suspend fun get(context: Context): String? {
        return context.applicationContext.userIdDataStore.data
            .map { it[USER_ID_KEY] }
            .first()
    }

    // ============ CREATION TIMESTAMP ============

    /**
     * Returns the account creation timestamp.
     * If it doesn't exist yet, captures the CURRENT time and stores it.
     * After that, it NEVER changes — even if the display name changes.
     */
    suspend fun getOrCreateCreationTimestamp(context: Context): Long {
        val store = context.applicationContext.userIdDataStore
        val existing = store.data.map { it[CREATION_TIMESTAMP_KEY] }.first()
        if (existing != null) return existing

        val now = System.currentTimeMillis()
        store.edit { it[CREATION_TIMESTAMP_KEY] = now }
        return now
    }

    // ============ DISPLAY NAME ============

    /**
     * Read the stored display name. Null if not set.
     */
    suspend fun getDisplayName(context: Context): String? {
        return context.applicationContext.userIdDataStore.data
            .map { it[DISPLAY_NAME_KEY] }
            .first()
    }

    /**
     * Read display name OR generate one if missing.
     * Uses the SAME creation timestamp on every regeneration,
     * so the number part stays consistent.
     */
    suspend fun getOrCreateDisplayName(context: Context): String {
        val store = context.applicationContext.userIdDataStore

        val existing = store.data.map { it[DISPLAY_NAME_KEY] }.first()
        if (existing != null && existing.isNotBlank()) return existing

        // Get or create the account timestamp
        val timestamp = getOrCreateCreationTimestamp(context)

        // Generate name using that timestamp
        val generated = DisplayNameGenerator.generate(timestamp)
        store.edit {
            it[DISPLAY_NAME_KEY] = generated
            it[IS_CUSTOM_NAME_KEY] = false
        }
        return generated
    }

    /**
     * User set their own name. Overwrites silent fallback.
     */
    suspend fun setDisplayName(context: Context, name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return
        context.applicationContext.userIdDataStore.edit {
            it[DISPLAY_NAME_KEY] = trimmed.take(30)
            it[IS_CUSTOM_NAME_KEY] = true
        }
    }

    /**
     * User cleared their name. Next read regenerates using the
     * SAME creation timestamp.
     */
    suspend fun clearDisplayName(context: Context) {
        context.applicationContext.userIdDataStore.edit {
            it.remove(DISPLAY_NAME_KEY)
            it[IS_CUSTOM_NAME_KEY] = false
        }
    }

    /**
     * True if user has set their own name.
     */
    suspend fun hasCustomName(context: Context): Boolean {
        return context.applicationContext.userIdDataStore.data
            .map { it[IS_CUSTOM_NAME_KEY] ?: false }
            .first()
    }
}
