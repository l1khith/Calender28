package com.l1khith.calender28.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath

@Volatile
private var dataStoreInstance: DataStore<Preferences>? = null

fun createDataStore(context: Context): DataStore<Preferences> {
    return dataStoreInstance ?: synchronized(DataStoreFactoryLock) {
        dataStoreInstance ?: PreferenceDataStoreFactory.createWithPath(
            produceFile = { context.applicationContext.filesDir.resolve("user_preferences.preferences_pb").absolutePath.toPath() }
        ).also { dataStoreInstance = it }
    }
}

private object DataStoreFactoryLock

